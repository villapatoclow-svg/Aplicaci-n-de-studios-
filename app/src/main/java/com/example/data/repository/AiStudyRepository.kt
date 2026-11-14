package com.example.data.repository

import android.graphics.Bitmap
import android.util.Log
import com.example.data.model.AiConnectorConfig
import com.example.data.model.AiDuelAgent
import com.example.data.model.ArtsGuideResult
import com.example.data.model.ChessPuzzle
import com.example.data.model.CodePlaygroundResult
import com.example.data.model.DuelChallenge
import com.example.data.model.ExamQuestion
import com.example.data.model.ExecutiveAdvisorResult
import com.example.data.model.FinanceEntry
import com.example.data.model.GeneratedExam
import com.example.data.model.HomeworkScanResult
import com.example.data.model.MatchPair
import com.example.data.model.PurchaseAdvice
import com.example.data.model.RoutePlanResult
import com.example.data.model.RouteStepInfo
import com.example.data.model.SchoolAutomationResult
import com.example.data.model.SchoolTaskDraft
import com.example.data.model.SchoolTeacherDraft
import com.example.data.model.SlideAnalysisResult
import com.example.data.model.SmartTask
import com.example.data.model.StepByStepSolution
import com.example.data.model.StudentProfile
import com.example.data.model.StudyModule
import com.example.data.model.SubjectRecord
import com.example.data.model.TutorMessage
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

data class GeneratedStudyPlanPayload(
    val title: String,
    val summary: String,
    val modules: List<StudyModule>
)

data class GeneratedSmartTaskPayload(
    val title: String,
    val subject: String,
    val description: String,
    val procedureSteps: List<String>,
    val xpReward: Long
)

data class AutomatedAgentBundle(
    val executiveBrief: String,
    val plan: GeneratedStudyPlanPayload,
    val tasks: List<GeneratedSmartTaskPayload>,
    val teachers: List<SchoolTeacherDraft>
)

class AiStudyRepository {

    private val externalDispatcher = ExternalAiDispatcher()

    @Volatile
    private var connectorConfig: AiConnectorConfig = AiConnectorConfig()

    fun updateConnectorConfig(config: AiConnectorConfig) {
        connectorConfig = config
    }

    suspend fun testExternalConnector(config: AiConnectorConfig): Result<String> {
        return externalDispatcher.testConnection(config)
    }

    private suspend fun executeJsonPrompt(
        prompt: String,
        schemaHint: String,
        model: GenerativeModel,
        bitmap: Bitmap? = null
    ): String {
        val cfg = connectorConfig
        if (externalDispatcher.isExternalTextActive(cfg) && (bitmap == null || cfg.activeProvider != "openai_compatible")) {
            return withTimeout(TIMEOUT_MS) {
                externalDispatcher.generateText(
                    config = cfg,
                    prompt = "$prompt\n\nFormato JSON exacto requerido: $schemaHint",
                    expectJson = true,
                    bitmap = bitmap
                )
            }
        }
        val response = withTimeout(TIMEOUT_MS) {
            if (bitmap != null) {
                model.generateContent(content { image(bitmap); text(prompt) })
            } else {
                model.generateContent(prompt)
            }
        }
        return response.text ?: throw IllegalStateException("La IA devolvió una respuesta vacía.")
    }

    private suspend fun executeTextPrompt(prompt: String, timeoutMs: Long = TIMEOUT_MS): String {
        val cfg = connectorConfig
        if (externalDispatcher.isExternalTextActive(cfg)) {
            return withTimeout(timeoutMs) {
                externalDispatcher.generateText(config = cfg, prompt = prompt, expectJson = false)
            }
        }
        val response = withTimeout(timeoutMs) {
            generalModel.generateContent(prompt)
        }
        return response.text.orEmpty()
    }

    private val generalModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash"
        )
    }

    private val solutionModel by lazy {
        val schema = Schema.obj(
            properties = mapOf(
                "finalAnswer" to Schema.string(description = "Respuesta final clara y concisa"),
                "subjectArea" to Schema.string(description = "Área científica o matemática detectada"),
                "formulaUsed" to Schema.string(description = "Fórmula o principio principal usado"),
                "steps" to Schema.array(
                    items = Schema.string(),
                    description = "Pasos detallados del procedimiento en español"
                ),
                "studyTip" to Schema.string(description = "Consejo breve para no olvidar este tema")
            )
        )
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = schema
            }
        )
    }

    private val planModel by lazy {
        val moduleSchema = Schema.obj(
            properties = mapOf(
                "title" to Schema.string(),
                "subject" to Schema.string(),
                "durationMinutes" to Schema.integer(),
                "activities" to Schema.array(items = Schema.string())
            )
        )
        val schema = Schema.obj(
            properties = mapOf(
                "title" to Schema.string(description = "Título motivador del plan"),
                "summary" to Schema.string(description = "Estrategia del plan priorizando materias bajas"),
                "modules" to Schema.array(items = moduleSchema)
            )
        )
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = schema
            }
        )
    }

    private val smartTaskModel by lazy {
        val schema = Schema.obj(
            properties = mapOf(
                "title" to Schema.string(),
                "subject" to Schema.string(),
                "description" to Schema.string(),
                "procedureSteps" to Schema.array(items = Schema.string()),
                "xpReward" to Schema.integer()
            )
        )
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = schema
            }
        )
    }

    private val examModel by lazy {
        val qSchema = Schema.obj(
            properties = mapOf(
                "question" to Schema.string(),
                "options" to Schema.array(items = Schema.string()),
                "correctIndex" to Schema.integer(),
                "explanation" to Schema.string()
            )
        )
        val schema = Schema.obj(
            properties = mapOf(
                "title" to Schema.string(),
                "questions" to Schema.array(items = qSchema)
            )
        )
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = schema
            }
        )
    }

    private val matchPairsModel by lazy {
        val pairSchema = Schema.obj(
            properties = mapOf(
                "term" to Schema.string(),
                "definition" to Schema.string()
            )
        )
        val schema = Schema.obj(
            properties = mapOf(
                "pairs" to Schema.array(items = pairSchema)
            )
        )
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = schema
            }
        )
    }

    private val slideModel by lazy {
        val schema = Schema.obj(
            properties = mapOf(
                "title" to Schema.string(),
                "keyPoints" to Schema.array(items = Schema.string()),
                "simpleExplanation" to Schema.string(),
                "suggestedTaskTitle" to Schema.string(),
                "suggestedTaskSteps" to Schema.array(items = Schema.string())
            )
        )
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = schema
            }
        )
    }

    private val schoolAutomationModel by lazy {
        val teacherSchema = Schema.obj(
            properties = mapOf(
                "name" to Schema.string(),
                "subject" to Schema.string(),
                "focusNote" to Schema.string(),
                "howToHelp" to Schema.string()
            )
        )
        val taskSchema = Schema.obj(
            properties = mapOf(
                "title" to Schema.string(),
                "subject" to Schema.string(),
                "description" to Schema.string(),
                "steps" to Schema.array(items = Schema.string())
            )
        )
        val schema = Schema.obj(
            properties = mapOf(
                "actionSummary" to Schema.string(),
                "suggestedTeachers" to Schema.array(items = teacherSchema),
                "suggestedTasks" to Schema.array(items = taskSchema)
            )
        )
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = schema
            }
        )
    }

    private val duelModel by lazy {
        val schema = Schema.obj(
            properties = mapOf(
                "question" to Schema.string(),
                "options" to Schema.array(items = Schema.string()),
                "correctIndex" to Schema.integer(),
                "agentChoiceIndex" to Schema.integer(),
                "agentTaunt" to Schema.string(),
                "explanation" to Schema.string()
            )
        )
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = schema
            }
        )
    }

    private val artsModel by lazy {
        val schema = Schema.obj(
            properties = mapOf(
                "projectTitle" to Schema.string(),
                "discipline" to Schema.string(),
                "materials" to Schema.array(items = Schema.string()),
                "paletteOrMood" to Schema.string(),
                "steps" to Schema.array(items = Schema.string()),
                "masteryTip" to Schema.string()
            )
        )
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = schema
            }
        )
    }

    private val automatedBundleModel by lazy {
        val moduleSchema = Schema.obj(
            properties = mapOf(
                "title" to Schema.string(),
                "subject" to Schema.string(),
                "durationMinutes" to Schema.integer(),
                "activities" to Schema.array(items = Schema.string())
            )
        )
        val taskSchema = Schema.obj(
            properties = mapOf(
                "title" to Schema.string(),
                "subject" to Schema.string(),
                "description" to Schema.string(),
                "procedureSteps" to Schema.array(items = Schema.string()),
                "xpReward" to Schema.integer()
            )
        )
        val teacherSchema = Schema.obj(
            properties = mapOf(
                "name" to Schema.string(),
                "subject" to Schema.string(),
                "focusNote" to Schema.string(),
                "howToHelp" to Schema.string()
            )
        )
        val schema = Schema.obj(
            properties = mapOf(
                "executiveBrief" to Schema.string(),
                "planTitle" to Schema.string(),
                "planSummary" to Schema.string(),
                "modules" to Schema.array(items = moduleSchema),
                "tasks" to Schema.array(items = taskSchema),
                "teachers" to Schema.array(items = teacherSchema)
            )
        )
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = schema
            }
        )
    }

    suspend fun solveStepByStep(problem: String, mode: String): Result<StepByStepSolution> {
        return try {
            val prompt = """
                Eres una calculadora científica y de ciencias avanzada para estudiantes en español.
                Modo seleccionado: $mode.
                Resuelve paso a paso el siguiente ejercicio o expresión: "$problem".
                Explica cada sustitución, unidad y regla matemática/científica con claridad.
            """.trimIndent()
            val raw = executeJsonPrompt(
                prompt = prompt,
                schemaHint = """{"finalAnswer":string,"subjectArea":string,"formulaUsed":string,"steps":[string],"studyTip":string}""",
                model = solutionModel
            )
            val json = JSONObject(raw)
            val stepsArr = json.getJSONArray("steps")
            val steps = (0 until stepsArr.length()).map { stepsArr.getString(it) }
            Result.success(
                StepByStepSolution(
                    finalAnswer = json.getString("finalAnswer"),
                    subjectArea = json.getString("subjectArea"),
                    formulaUsed = json.getString("formulaUsed"),
                    steps = steps,
                    studyTip = json.getString("studyTip")
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Calculator AI timeout", e)
            Result.failure(Exception("Tiempo agotado al calcular el procedimiento. Intenta de nuevo."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Calculator AI error", e)
            Result.failure(e)
        }
    }

    suspend fun generateStudyPlan(
        academicLevel: String,
        subjects: List<SubjectRecord>,
        dailyMinutes: Int,
        goal: String
    ): Result<GeneratedStudyPlanPayload> {
        return try {
            val subjectsDesc = if (subjects.isEmpty()) {
                "Matemáticas (6.0 - reforzar), Ciencias (7.5 - estable), Historia (9.0 - fuerte)"
            } else {
                subjects.joinToString("; ") { "${it.name} (calificación ${it.grade}, prioridad ${it.priority})" }
            }
            val prompt = """
                Diseña un plan estudiantil completo y accionable en español para un estudiante de nivel "$academicLevel".
                Materias y rendimiento: $subjectsDesc.
                Tiempo diario disponible: $dailyMinutes minutos.
                Objetivo principal: $goal.
                Crea entre 4 y 6 módulos estructurados priorizando primero las materias con prioridad "reforzar" sin descuidar las demás. Cada módulo debe incluir 2 o 3 actividades concretas.
            """.trimIndent()

            val raw = executeJsonPrompt(
                prompt = prompt,
                schemaHint = """{"title":string,"summary":string,"modules":[{"title":string,"subject":string,"durationMinutes":number,"activities":[string]}]}""",
                model = planModel
            )
            val json = JSONObject(raw)
            val modsArr = json.getJSONArray("modules")
            val modules = (0 until modsArr.length()).map { i ->
                val obj = modsArr.getJSONObject(i)
                val actsArr = obj.getJSONArray("activities")
                val acts = (0 until actsArr.length()).map { actsArr.getString(it) }
                StudyModule(
                    title = obj.getString("title"),
                    subject = obj.getString("subject"),
                    durationMinutes = obj.optLong("durationMinutes", 35L).coerceIn(15L, 180L),
                    completed = false,
                    activities = acts
                )
            }
            Result.success(
                GeneratedStudyPlanPayload(
                    title = json.getString("title"),
                    summary = json.getString("summary"),
                    modules = modules
                )
            )
        } catch (e: TimeoutCancellationException) {
            Result.failure(Exception("La generación del plan tardó demasiado. Intenta de nuevo."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Study plan AI error", e)
            Result.failure(e)
        }
    }

    suspend fun generateSmartTask(
        userRequest: String,
        preferredSubject: String,
        academicLevel: String
    ): Result<GeneratedSmartTaskPayload> {
        return try {
            val prompt = """
                El estudiante de nivel $academicLevel te pide esta tarea inteligente: "$userRequest".
                Materia sugerida: $preferredSubject.
                Genera una tarea bien organizada con un título claro, descripción útil y entre 4 y 6 pasos concretos del procedimiento para resolverla o elaborarla desde cero.
                Asigna xpReward entre 30 y 80 según la dificultad.
            """.trimIndent()
            val raw = executeJsonPrompt(
                prompt = prompt,
                schemaHint = """{"title":string,"subject":string,"description":string,"procedureSteps":[string],"xpReward":number}""",
                model = smartTaskModel
            )
            val json = JSONObject(raw)
            val stepsArr = json.getJSONArray("procedureSteps")
            val steps = (0 until stepsArr.length()).map { stepsArr.getString(it) }
            Result.success(
                GeneratedSmartTaskPayload(
                    title = json.getString("title"),
                    subject = json.getString("subject"),
                    description = json.getString("description"),
                    procedureSteps = steps,
                    xpReward = json.optLong("xpReward", 45L).coerceIn(20L, 120L)
                )
            )
        } catch (e: TimeoutCancellationException) {
            Result.failure(Exception("Tiempo agotado creando la tarea inteligente."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Smart task AI error", e)
            Result.failure(e)
        }
    }

    suspend fun generateExam(
        subject: String,
        topic: String,
        difficulty: String,
        questionCount: Int,
        academicLevel: String
    ): Result<GeneratedExam> {
        return try {
            val count = questionCount.coerceIn(3, 10)
            val prompt = """
                Genera un examen en español para nivel $academicLevel sobre la materia "$subject" y el tema "$topic".
                Dificultad: $difficulty. Número exacto de preguntas: $count.
                Cada pregunta debe tener exactamente 4 opciones, un correctIndex entre 0 y 3, y una explicación pedagógica clara.
            """.trimIndent()
            val raw = executeJsonPrompt(
                prompt = prompt,
                schemaHint = """{"title":string,"questions":[{"question":string,"options":[string,string,string,string],"correctIndex":number,"explanation":string}]}""",
                model = examModel
            )
            val json = JSONObject(raw)
            val qArr = json.getJSONArray("questions")
            val questions = (0 until qArr.length()).map { i ->
                val obj = qArr.getJSONObject(i)
                val optsArr = obj.getJSONArray("options")
                val opts = (0 until optsArr.length()).map { optsArr.getString(it) }
                ExamQuestion(
                    question = obj.getString("question"),
                    options = opts,
                    correctIndex = obj.getInt("correctIndex").coerceIn(0, (opts.size - 1).coerceAtLeast(0)),
                    explanation = obj.getString("explanation")
                )
            }
            Result.success(
                GeneratedExam(
                    title = json.getString("title"),
                    subject = subject,
                    difficulty = difficulty,
                    questions = questions
                )
            )
        } catch (e: TimeoutCancellationException) {
            Result.failure(Exception("Tiempo agotado generando el examen."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Exam AI error", e)
            Result.failure(e)
        }
    }

    suspend fun generateMatchPairs(subject: String, academicLevel: String): Result<List<MatchPair>> {
        return try {
            val prompt = """
                Crea exactamente 4 pares de concepto y definición corta (máximo 8 palabras por definición) para un minijuego de memoria de "$subject" (nivel $academicLevel) en español.
            """.trimIndent()
            val raw = executeJsonPrompt(
                prompt = prompt,
                schemaHint = """{"pairs":[{"term":string,"definition":string}]}""",
                model = matchPairsModel
            )
            val json = JSONObject(raw)
            val arr = json.getJSONArray("pairs")
            val pairs = (0 until arr.length()).map { idx ->
                val obj = arr.getJSONObject(idx)
                MatchPair(
                    id = "pair_$idx",
                    term = obj.getString("term"),
                    definition = obj.getString("definition")
                )
            }
            Result.success(pairs)
        } catch (e: TimeoutCancellationException) {
            Result.failure(Exception("Tiempo agotado generando el minijuego."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Match pairs AI error", e)
            Result.failure(e)
        }
    }

    suspend fun analyzeSlideBitmap(bitmap: Bitmap, questionHint: String): Result<SlideAnalysisResult> {
        return try {
            val extra = if (questionHint.isNotBlank()) "Duda específica del estudiante: $questionHint." else ""
            val slidePrompt = """
                Analiza esta diapositiva, apunte o pizarrón de clase en español.
                Extrae el título del tema, los puntos clave más importantes, una explicación sencilla para estudiarlo rápido, y sugiere una tarea de repaso con 3-4 pasos.
                $extra
            """.trimIndent()
            val raw = executeJsonPrompt(
                prompt = slidePrompt,
                schemaHint = """{"title":string,"keyPoints":[string],"simpleExplanation":string,"suggestedTaskTitle":string,"suggestedTaskSteps":[string]}""",
                model = slideModel,
                bitmap = bitmap
            )
            val json = JSONObject(raw)
            val kpArr = json.getJSONArray("keyPoints")
            val keyPoints = (0 until kpArr.length()).map { kpArr.getString(it) }
            val stepsArr = json.getJSONArray("suggestedTaskSteps")
            val taskSteps = (0 until stepsArr.length()).map { stepsArr.getString(it) }
            Result.success(
                SlideAnalysisResult(
                    title = json.getString("title"),
                    keyPoints = keyPoints,
                    simpleExplanation = json.getString("simpleExplanation"),
                    suggestedTaskTitle = json.getString("suggestedTaskTitle"),
                    suggestedTaskSteps = taskSteps
                )
            )
        } catch (e: TimeoutCancellationException) {
            Result.failure(Exception("El análisis de diapositiva excedió el tiempo límite."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Slide vision AI error", e)
            Result.failure(e)
        }
    }

    // --- New: School / Secundaria News & Teachers Automation ---

    suspend fun analyzeSchoolAndTeachers(
        schoolName: String,
        schoolNews: String,
        academicLevel: String,
        subjects: List<SubjectRecord>
    ): Result<SchoolAutomationResult> {
        return try {
            val subjList = subjects.joinToString(", ") { "${it.name} (${it.grade})" }
                .ifBlank { "Matemáticas, Ciencias, Español, Artes, Historia" }
            val prompt = """
                El estudiante asiste a la institución "$schoolName" (nivel $academicLevel).
                Noticias / avisos escolares recientes: "$schoolNews".
                Sus materias actuales: $subjList.
                1. Escribe un actionSummary breve explicando qué priorizar según las noticias de la escuela.
                2. Sugiere entre 2 y 3 perfiles de profesores (name como "Prof./Mtra. según materia", subject, focusNote ligada a las noticias escolares, y howToHelp con acciones concretas sobre cómo el estudiante puede ayudar al profesor o destacar positivamente en clase).
                3. Sugiere 2 tareas inteligentes urgentes alineadas con los avisos escolares, cada una con 3 pasos concretos.
                Todo en español claro y motivador.
            """.trimIndent()
            val raw = executeJsonPrompt(
                prompt = prompt,
                schemaHint = """{"actionSummary":string,"suggestedTeachers":[{"name":string,"subject":string,"focusNote":string,"howToHelp":string}],"suggestedTasks":[{"title":string,"subject":string,"description":string,"steps":[string]}]}""",
                model = schoolAutomationModel
            )
            val json = JSONObject(raw)
            val tArr = json.getJSONArray("suggestedTeachers")
            val teachers = (0 until tArr.length()).map { i ->
                val obj = tArr.getJSONObject(i)
                SchoolTeacherDraft(
                    name = obj.getString("name"),
                    subject = obj.getString("subject"),
                    focusNote = obj.getString("focusNote"),
                    howToHelp = obj.getString("howToHelp")
                )
            }
            val taskArr = json.getJSONArray("suggestedTasks")
            val tasks = (0 until taskArr.length()).map { i ->
                val obj = taskArr.getJSONObject(i)
                val sArr = obj.getJSONArray("steps")
                val steps = (0 until sArr.length()).map { sArr.getString(it) }
                SchoolTaskDraft(
                    title = obj.getString("title"),
                    subject = obj.getString("subject"),
                    description = obj.getString("description"),
                    steps = steps
                )
            }
            Result.success(
                SchoolAutomationResult(
                    actionSummary = json.getString("actionSummary"),
                    suggestedTeachers = teachers,
                    suggestedTasks = tasks
                )
            )
        } catch (e: TimeoutCancellationException) {
            Result.failure(Exception("Tiempo agotado al automatizar noticias de tu escuela."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "School automation AI error", e)
            Result.failure(e)
        }
    }

    // --- New: Head-to-Head AI Duel Challenges ---

    suspend fun generateDuelRound(
        agent: AiDuelAgent,
        academicLevel: String,
        roundNumber: Int,
        previousQuestions: List<String>
    ): Result<DuelChallenge> {
        return try {
            val prevNote = if (previousQuestions.isNotEmpty()) {
                "No repitas estas preguntas previas: ${previousQuestions.joinToString(" | ")}."
            } else ""
            val prompt = """
                Eres "${agent.name}" (${agent.title}), un agente rival de IA especialista en ${agent.subject}.
                Estás en un duelo académico ronda #$roundNumber contra un estudiante de nivel $academicLevel.
                $prevNote
                Genera 1 reto intelectual desafiante pero justo en español de ${agent.subject} con exactamente 4 opciones, correctIndex (0..3), agentChoiceIndex (0..3, la respuesta que tú como agente eliges: acierta la mayoría de veces ~75% para ser un rival competitivo pero vencible), agentTaunt (frase competitiva amigable de máx 15 palabras), y explanation (por qué es correcta).
            """.trimIndent()
            val raw = executeJsonPrompt(
                prompt = prompt,
                schemaHint = """{"question":string,"options":[string,string,string,string],"correctIndex":number,"agentChoiceIndex":number,"agentTaunt":string,"explanation":string}""",
                model = duelModel
            )
            val json = JSONObject(raw)
            val optsArr = json.getJSONArray("options")
            val opts = (0 until optsArr.length()).map { optsArr.getString(it) }
            val maxIdx = (opts.size - 1).coerceAtLeast(0)
            Result.success(
                DuelChallenge(
                    question = json.getString("question"),
                    options = opts,
                    correctIndex = json.getInt("correctIndex").coerceIn(0, maxIdx),
                    agentChoiceIndex = json.getInt("agentChoiceIndex").coerceIn(0, maxIdx),
                    agentTaunt = json.getString("agentTaunt"),
                    explanation = json.getString("explanation")
                )
            )
        } catch (e: TimeoutCancellationException) {
            Result.failure(Exception("El agente rival tardó demasiado en responder."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Duel AI error", e)
            Result.failure(e)
        }
    }

    // --- New: Arts & Creative Subjects Studio ---

    suspend fun generateArtsHelp(
        discipline: String,
        topic: String,
        academicLevel: String
    ): Result<ArtsGuideResult> {
        return try {
            val prompt = """
                Eres un maestro creativo de Artes en MentePro para nivel $academicLevel.
                Disciplina: "$discipline".
                Proyecto o duda del estudiante: "$topic".
                Responde en español con una guía creativa práctica: projectTitle, discipline, lista de 3-5 materiales accesibles, paletteOrMood (paleta cromática, ritmo o tono expresivo recomendado), 4-5 pasos claros de ejecución, y masteryTip para destacar ante su profesor de Artes.
            """.trimIndent()
            val raw = executeJsonPrompt(
                prompt = prompt,
                schemaHint = """{"projectTitle":string,"discipline":string,"materials":[string],"paletteOrMood":string,"steps":[string],"masteryTip":string}""",
                model = artsModel
            )
            val json = JSONObject(raw)
            val matsArr = json.getJSONArray("materials")
            val mats = (0 until matsArr.length()).map { matsArr.getString(it) }
            val stepsArr = json.getJSONArray("steps")
            val steps = (0 until stepsArr.length()).map { stepsArr.getString(it) }
            Result.success(
                ArtsGuideResult(
                    projectTitle = json.getString("projectTitle"),
                    discipline = json.getString("discipline"),
                    materials = mats,
                    paletteOrMood = json.getString("paletteOrMood"),
                    steps = steps,
                    masteryTip = json.getString("masteryTip")
                )
            )
        } catch (e: TimeoutCancellationException) {
            Result.failure(Exception("Tiempo agotado generando la guía artística."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Arts AI error", e)
            Result.failure(e)
        }
    }

    // --- New: Monthly Premium Automated Agent (1-tap full automation) ---

    suspend fun runPremiumAutomatedAgent(
        profile: StudentProfile,
        subjects: List<SubjectRecord>,
        schoolName: String,
        schoolNews: String
    ): Result<AutomatedAgentBundle> {
        return try {
            val subjectsDesc = if (subjects.isEmpty()) {
                "Matemáticas (6.5 - reforzar), Ciencias (7.8 - estable), Artes (9.0 - fuerte)"
            } else {
                subjects.joinToString("; ") { "${it.name} (${it.grade}, ${it.priority})" }
            }
            val schoolCtx = if (schoolName.isNotBlank() || schoolNews.isNotBlank()) {
                "Escuela: $schoolName. Noticias escolares: $schoolNews."
            } else "Sin noticias escolares específicas."

            val prompt = """
                Eres el Agente Premium Automatizado de MentePro trabajando en piloto automático para ${profile.displayName} (nivel ${profile.academicLevel}).
                Materias: $subjectsDesc.
                $schoolCtx
                Ejecuta una automatización integral en español:
                1. executiveBrief: resumen claro (3 líneas) de lo que automatizaste hoy.
                2. planTitle, planSummary y 4 módulos priorizando materias bajas y eventos escolares.
                3. 2 tareas inteligentes listas con procedureSteps (3 pasos c/u) y xpReward (40..70).
                4. 2 consejos de profesores (name, subject, focusNote, howToHelp) sobre cómo ayudarles y sumar puntos.
            """.trimIndent()

            val raw = executeJsonPrompt(
                prompt = prompt,
                schemaHint = """{"executiveBrief":string,"planTitle":string,"planSummary":string,"modules":[{"title":string,"subject":string,"durationMinutes":number,"activities":[string]}],"tasks":[{"title":string,"subject":string,"description":string,"procedureSteps":[string],"xpReward":number}],"teachers":[{"name":string,"subject":string,"focusNote":string,"howToHelp":string}]}""",
                model = automatedBundleModel
            )
            val json = JSONObject(raw)

            val modsArr = json.getJSONArray("modules")
            val modules = (0 until modsArr.length()).map { i ->
                val obj = modsArr.getJSONObject(i)
                val actsArr = obj.getJSONArray("activities")
                StudyModule(
                    title = obj.getString("title"),
                    subject = obj.getString("subject"),
                    durationMinutes = obj.optLong("durationMinutes", 35L).coerceIn(15L, 120L),
                    activities = (0 until actsArr.length()).map { actsArr.getString(it) }
                )
            }

            val tasksArr = json.getJSONArray("tasks")
            val tasks = (0 until tasksArr.length()).map { i ->
                val obj = tasksArr.getJSONObject(i)
                val stepsArr = obj.getJSONArray("procedureSteps")
                GeneratedSmartTaskPayload(
                    title = obj.getString("title"),
                    subject = obj.getString("subject"),
                    description = obj.getString("description"),
                    procedureSteps = (0 until stepsArr.length()).map { stepsArr.getString(it) },
                    xpReward = obj.optLong("xpReward", 55L).coerceIn(30L, 100L)
                )
            }

            val teachArr = json.getJSONArray("teachers")
            val teachers = (0 until teachArr.length()).map { i ->
                val obj = teachArr.getJSONObject(i)
                SchoolTeacherDraft(
                    name = obj.getString("name"),
                    subject = obj.getString("subject"),
                    focusNote = obj.getString("focusNote"),
                    howToHelp = obj.getString("howToHelp")
                )
            }

            Result.success(
                AutomatedAgentBundle(
                    executiveBrief = json.getString("executiveBrief"),
                    plan = GeneratedStudyPlanPayload(
                        title = json.getString("planTitle"),
                        summary = json.getString("planSummary"),
                        modules = modules
                    ),
                    tasks = tasks,
                    teachers = teachers
                )
            )
        } catch (e: TimeoutCancellationException) {
            Result.failure(Exception("El Agente Automatizado tardó demasiado. Reintenta."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Premium Automated Agent error", e)
            Result.failure(e)
        }
    }

    fun streamTutorReply(
        history: List<TutorMessage>,
        userMessage: String,
        academicLevel: String,
        weakSubjects: List<String>
    ): Flow<String> = flow {
        val weakNote = if (weakSubjects.isNotEmpty()) {
            "Materias donde necesita más apoyo: ${weakSubjects.joinToString(", ")}."
        } else ""
        val systemContext = """
            Eres la Tutora IA de MentePro, amigable, clara y experta en enseñanza paso a paso en español.
            Nivel del estudiante: $academicLevel. $weakNote
            Responde de forma didáctica, usando ejemplos claros y pasos numerados cuando expliques procedimientos.
        """.trimIndent()

        val cfg = connectorConfig
        if (externalDispatcher.isExternalTextActive(cfg)) {
            val recentTurns = history.takeLast(6).joinToString("\n") {
                val role = if (it.isFromUser) "Estudiante" else "Tutora"
                "$role: ${it.text}"
            }
            val fullPrompt = "$systemContext\n\nHistorial reciente:\n$recentTurns\n\nEstudiante: $userMessage\nTutora:"
            val reply = withTimeout(TIMEOUT_MS) {
                externalDispatcher.generateText(cfg, fullPrompt, expectJson = false)
            }
            if (reply.isBlank()) throw IllegalStateException("La Tutora IA externa no devolvió respuesta.")
            emit(reply)
            return@flow
        }

        val chatHistory = listOf(
            content("user") { text(systemContext) },
            content("model") { text("¡Hola! Estoy lista para ayudarte a dominar cualquier materia paso a paso.") }
        ) + history.takeLast(8).map { msg ->
            content(if (msg.isFromUser) "user" else "model") { text(msg.text) }
        }

        val chat = generalModel.startChat(history = chatHistory)
        val accumulated = StringBuilder()
        withTimeout(TIMEOUT_MS) {
            chat.sendMessageStream(userMessage).collect { chunk ->
                val text = chunk.text.orEmpty()
                if (text.isNotEmpty()) {
                    accumulated.append(text)
                    emit(accumulated.toString())
                }
            }
        }
        if (accumulated.isBlank()) {
            throw IllegalStateException("La Tutora IA no devolvió respuesta.")
        }
    }

    suspend fun generatePetEncouragement(
        petName: String,
        petLevel: Long,
        weakSubject: String?,
        pendingTasks: Int
    ): Result<String> {
        return try {
            val weakText = weakSubject?.let { "Su materia por reforzar es $it." } ?: "Va equilibrado en sus materias."
            val prompt = """
                Actúa como "$petName", una mascota compañera de estudios de nivel $petLevel.
                El estudiante tiene $pendingTasks tareas pendientes. $weakText
                Escribe un mensaje motivador corto (máximo 25 palabras) en español con un consejo práctico de estudio.
            """.trimIndent()
            val text = executeTextPrompt(prompt, 30_000L)
            Result.success(text.trim().ifBlank { "¡Cada tarea completada nos hace subir de nivel juntos!" })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Pet coach AI error", e)
            Result.failure(e)
        }
    }

    suspend fun explainChessMove(puzzle: ChessPuzzle, userQuestion: String): Result<String> {
        return try {
            val prompt = """
                Eres un entrenador de ajedrez didáctico para estudiantes.
                Posición FEN: ${puzzle.boardFen}. Turno: ${puzzle.sideToMove}.
                Tema: ${puzzle.theme}. Jugada ganadora: ${puzzle.solutionMoveSan}.
                Pregunta del estudiante: "$userQuestion".
                Responde en español en máximo 4 líneas claras y educativas.
            """.trimIndent()
            val text = executeTextPrompt(prompt, 30_000L)
            Result.success(text.trim().ifBlank { puzzle.explanation })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Chess coach AI error", e)
            Result.failure(e)
        }
    }

    // --- Versión 2 AI Models & Capabilities ---

    private val homeworkCameraModel by lazy {
        val schema = Schema.obj(
            properties = mapOf(
                "subject" to Schema.string(),
                "problemStatement" to Schema.string(),
                "finalAnswer" to Schema.string(),
                "steps" to Schema.array(items = Schema.string()),
                "verificationTip" to Schema.string(),
                "suggestedTaskTitle" to Schema.string()
            )
        )
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = schema
            }
        )
    }

    private val routePlannerModel by lazy {
        val stepSchema = Schema.obj(
            properties = mapOf(
                "instruction" to Schema.string(),
                "streetOrLine" to Schema.string(),
                "distanceOrDuration" to Schema.string()
            )
        )
        val schema = Schema.obj(
            properties = mapOf(
                "totalEstimatedTime" to Schema.string(),
                "estimatedCost" to Schema.string(),
                "routeSteps" to Schema.array(items = stepSchema),
                "streetHighlights" to Schema.array(items = Schema.string()),
                "tips" to Schema.string()
            )
        )
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = schema
            }
        )
    }

    private val executiveAdvisorModel by lazy {
        val adviceSchema = Schema.obj(
            properties = mapOf(
                "item" to Schema.string(),
                "estimatedCost" to Schema.string(),
                "reason" to Schema.string()
            )
        )
        val schema = Schema.obj(
            properties = mapOf(
                "dailyPriorityPlan" to Schema.string(),
                "buyList" to Schema.array(items = adviceSchema),
                "avoidBuyingList" to Schema.array(items = adviceSchema),
                "automationActions" to Schema.array(items = Schema.string())
            )
        )
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = schema
            }
        )
    }

    private val codePlaygroundModel by lazy {
        val schema = Schema.obj(
            properties = mapOf(
                "title" to Schema.string(),
                "summary" to Schema.string(),
                "htmlPreview" to Schema.string(),
                "codeSnippet" to Schema.string(),
                "instructions" to Schema.string()
            )
        )
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = schema
            }
        )
    }

    suspend fun solveHomeworkBitmap(bitmap: Bitmap, hint: String): Result<HomeworkScanResult> {
        return try {
            val extra = if (hint.isNotBlank()) "Indicación del estudiante: $hint." else ""
            val hwPrompt = """
                Eres un profesor experto de MentePro analizando la foto de una tarea escolar o ejercicio en español.
                1. Identifica la materia (subject) y transcribe brevemente el enunciado visible (problemStatement).
                2. Resuelve el ejercicio completo paso a paso (steps: 3 a 6 pasos claros y numerados con fórmulas o análisis).
                3. Da la respuesta final exacta (finalAnswer).
                4. Incluye un verificationTip para comprobar el resultado y un suggestedTaskTitle corto para guardarlo como tarea.
                $extra
            """.trimIndent()
            val raw = executeJsonPrompt(
                prompt = hwPrompt,
                schemaHint = """{"subject":string,"problemStatement":string,"finalAnswer":string,"steps":[string],"verificationTip":string,"suggestedTaskTitle":string}""",
                model = homeworkCameraModel,
                bitmap = bitmap
            )
            val json = JSONObject(raw)
            val stepsArr = json.getJSONArray("steps")
            Result.success(
                HomeworkScanResult(
                    subject = json.getString("subject"),
                    problemStatement = json.getString("problemStatement"),
                    finalAnswer = json.getString("finalAnswer"),
                    steps = (0 until stepsArr.length()).map { stepsArr.getString(it) },
                    verificationTip = json.getString("verificationTip"),
                    suggestedTaskTitle = json.getString("suggestedTaskTitle")
                )
            )
        } catch (e: TimeoutCancellationException) {
            Result.failure(Exception("El análisis de la tarea con cámara tardó demasiado."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Homework camera AI error", e)
            Result.failure(e)
        }
    }

    suspend fun generateUniversityRoute(
        origin: String,
        destination: String,
        transportMode: String,
        degreeOrMaster: String
    ): Result<RoutePlanResult> {
        return try {
            val degreeNote = if (degreeOrMaster.isNotBlank()) "Estudiante de: $degreeOrMaster." else ""
            val prompt = """
                Eres un asesor de movilidad urbana y rutas universitarias en español.
                Origen: "$origin".
                Destino / Universidad: "$destination".
                Modo de transporte elegido: "$transportMode". $degreeNote
                Traza una ruta clara aunque el destino esté lejos:
                - totalEstimatedTime: tiempo estimado realista.
                - estimatedCost: costo aproximado de pasaje o gasolina.
                - routeSteps: lista de 4 a 7 tramos detallados con instruction, streetOrLine (calle, avenida principal, línea o estación) y distanceOrDuration.
                - streetHighlights: 3 a 5 avenidas o vialidades clave del recorrido.
                - tips: consejo práctico de horario, transbordo y seguridad para llegar puntual.
            """.trimIndent()
            val raw = executeJsonPrompt(
                prompt = prompt,
                schemaHint = """{"totalEstimatedTime":string,"estimatedCost":string,"routeSteps":[{"instruction":string,"streetOrLine":string,"distanceOrDuration":string}],"streetHighlights":[string],"tips":string}""",
                model = routePlannerModel
            )
            val json = JSONObject(raw)
            val stepsArr = json.getJSONArray("routeSteps")
            val routeSteps = (0 until stepsArr.length()).map { i ->
                val obj = stepsArr.getJSONObject(i)
                RouteStepInfo(
                    instruction = obj.getString("instruction"),
                    streetOrLine = obj.getString("streetOrLine"),
                    distanceOrDuration = obj.getString("distanceOrDuration")
                )
            }
            val streetsArr = json.getJSONArray("streetHighlights")
            Result.success(
                RoutePlanResult(
                    origin = origin,
                    destination = destination,
                    transportMode = transportMode,
                    totalEstimatedTime = json.getString("totalEstimatedTime"),
                    estimatedCost = json.getString("estimatedCost"),
                    routeSteps = routeSteps,
                    streetHighlights = (0 until streetsArr.length()).map { streetsArr.getString(it) },
                    tips = json.getString("tips")
                )
            )
        } catch (e: TimeoutCancellationException) {
            Result.failure(Exception("La generación de ruta tardó demasiado. Intenta de nuevo."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Route planner AI error", e)
            Result.failure(e)
        }
    }

    suspend fun generateExecutiveAdvisor(
        profile: StudentProfile,
        finances: List<FinanceEntry>,
        tasks: List<SmartTask>,
        decisionQuery: String
    ): Result<ExecutiveAdvisorResult> {
        return try {
            val totalIncome = finances.filter { it.entryType == "ingreso" }.sumOf { it.amount }
            val totalExpense = finances.filter { it.entryType == "gasto" }.sumOf { it.amount }
            val totalAssets = finances.filter { it.entryType == "bien" }.sumOf { it.amount }
            val pendingTitles = tasks.filter { !it.completed }.take(5).joinToString("; ") { "${it.title} (${it.subject})" }
                .ifBlank { "Sin tareas pendientes registradas" }
            val queryLine = if (decisionQuery.isNotBlank()) "Consulta o compra que evalúa el usuario: \"$decisionQuery\"." else ""

            val prompt = """
                Eres el Asistente Personal Ejecutivo (Plan Alto 18+) de ${profile.displayName} (${profile.ageYears} años, nivel ${profile.academicLevel}, programa/maestría: "${profile.degreeOrMaster.ifBlank { "Estudios Superiores" }}").
                Estado financiero real: Ingresos=$totalIncome, Gastos=$totalExpense, Bienes/Patrimonio=$totalAssets, Balance=${totalIncome - totalExpense}.
                Tareas pendientes: $pendingTitles.
                $queryLine
                Responde en español serio, claro y estratégico:
                1. dailyPriorityPlan: plan ejecutivo del día (3 a 4 líneas directas).
                2. buyList: 2 a 3 cosas que SÍ conviene comprar o invertir (con item, estimatedCost y reason práctica).
                3. avoidBuyingList: 2 a 3 cosas que NO debe comprar y POR QUÉ no comprarlas ahora (evitando gastos hormiga, duplicados o fuera de presupuesto).
                4. automationActions: 3 acciones automatizables concretas para agregar a su agenda de hoy.
            """.trimIndent()
            val raw = executeJsonPrompt(
                prompt = prompt,
                schemaHint = """{"dailyPriorityPlan":string,"buyList":[{"item":string,"estimatedCost":string,"reason":string}],"avoidBuyingList":[{"item":string,"estimatedCost":string,"reason":string}],"automationActions":[string]}""",
                model = executiveAdvisorModel
            )
            val json = JSONObject(raw)

            fun parseAdviceList(key: String): List<PurchaseAdvice> {
                val arr = json.getJSONArray(key)
                return (0 until arr.length()).map { i ->
                    val o = arr.getJSONObject(i)
                    PurchaseAdvice(
                        item = o.getString("item"),
                        estimatedCost = o.getString("estimatedCost"),
                        reason = o.getString("reason")
                    )
                }
            }

            val actionsArr = json.getJSONArray("automationActions")
            Result.success(
                ExecutiveAdvisorResult(
                    dailyPriorityPlan = json.getString("dailyPriorityPlan"),
                    buyList = parseAdviceList("buyList"),
                    avoidBuyingList = parseAdviceList("avoidBuyingList"),
                    automationActions = (0 until actionsArr.length()).map { actionsArr.getString(it) }
                )
            )
        } catch (e: TimeoutCancellationException) {
            Result.failure(Exception("El asistente ejecutivo tardó demasiado en responder."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Executive advisor AI error", e)
            Result.failure(e)
        }
    }

    suspend fun generateInstantPlayableCode(
        prompt: String,
        kind: String
    ): Result<CodePlaygroundResult> {
        return try {
            val modeLabel = if (kind == "game") "VIDEOJUEGO HTML5 INTERACTIVO" else "APLICACIÓN WEB INTERACTIVA"
            val fullPrompt = """
                Eres un ingeniero creativo de software y diseñador de videojuegos en MentePro.
                Crea al instante un $modeLabel basado en esta idea del usuario: "$prompt".
                REQUISITOS ESTRICTOS para htmlPreview:
                - Debe ser un documento HTML5 completo y autónomo (<!DOCTYPE html><html>...</html>) con <style> y <script> inline.
                - PROHIBIDO usar librerías externas, CDN, fetch, imágenes externas o localStorage. Todo dibujado con Canvas 2D o DOM/SVG puro.
                - Diseño oscuro elegante y moderno, adaptado a pantalla móvil (meta viewport width=device-width, initial-scale=1.0, user-scalable=no).
                - Si es videojuego: DEBE incluir controles táctiles grandes en pantalla (botones direccionales / acción touchstart+click), marcador de puntos, botón Reiniciar y lógica fluida con requestAnimationFrame.
                - Si es aplicación: DEBE incluir botones interactivos reales, inputs funcionales y retroalimentación visual inmediata en pantalla.
                También entrega:
                - title: nombre atractivo.
                - summary: explicación en español de qué construiste y su lógica.
                - codeSnippet: fragmento limpio o arquitectura clave explicada (máx 45 líneas).
                - instructions: cómo jugarlo o probarlo paso a paso.
            """.trimIndent()
            val raw = executeJsonPrompt(
                prompt = fullPrompt,
                schemaHint = """{"title":string,"summary":string,"htmlPreview":string,"codeSnippet":string,"instructions":string}""",
                model = codePlaygroundModel
            )
            val json = JSONObject(raw)
            Result.success(
                CodePlaygroundResult(
                    title = json.getString("title"),
                    kind = kind,
                    summary = json.getString("summary"),
                    htmlPreview = json.getString("htmlPreview"),
                    codeSnippet = json.getString("codeSnippet"),
                    instructions = json.getString("instructions")
                )
            )
        } catch (e: TimeoutCancellationException) {
            Result.failure(Exception("La creación del código instantáneo tardó demasiado. Intenta con una idea más breve."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Code playground AI error", e)
            Result.failure(e)
        }
    }

    private companion object {
        const val TAG = "AiStudyRepository"
        const val TIMEOUT_MS = 60_000L
    }
}
