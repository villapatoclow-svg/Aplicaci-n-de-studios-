package com.example.domain

import com.example.data.model.AiDuelAgent
import com.example.data.model.SubjectCatalogModule

object SubjectCatalog {

    val academicLevels = listOf("Primaria", "Secundaria", "Preparatoria", "Universidad")

    val duelAgents = listOf(
        AiDuelAgent(
            id = "gauss",
            name = "Dr. Gauss IA",
            title = "Gran Maestro Matemático",
            subject = "Matemáticas",
            tagline = "Especialista en álgebra, cálculo, geometría y cálculo mental veloz.",
            levelLabel = "Experto STEM"
        ),
        AiDuelAgent(
            id = "curie",
            name = "Dra. Curie IA",
            title = "Estratega de Ciencias",
            subject = "Física y Química",
            tagline = "Domina leyes de Newton, estequiometría, energía y biología celular.",
            levelLabel = "Laboratorio Pro"
        ),
        AiDuelAgent(
            id = "davinci",
            name = "Maestro Da Vinci IA",
            title = "Mentor de Artes y Humanidades",
            subject = "Artes y Cultura",
            tagline = "Desafíos sobre composición, color, historia del arte, música y literatura.",
            levelLabel = "Estudio Creativo"
        ),
        AiDuelAgent(
            id = "ada",
            name = "Ada Byte IA",
            title = "Arquitecta de Algoritmos",
            subject = "Programación e Ingeniería",
            tagline = "Retos de lógica computacional, estructuras de datos y pensamiento analítico.",
            levelLabel = "Nivel Universitario"
        )
    )

    val artsDisciplines = listOf(
        "Dibujo y Pintura",
        "Música y Ritmo",
        "Teatro y Expresión",
        "Literatura Creativa",
        "Diseño y Arquitectura"
    )

    val modules: List<SubjectCatalogModule> = listOf(
        // --- PRIMARIA ---
        SubjectCatalogModule(
            id = "pri_math",
            level = "Primaria",
            name = "Matemáticas Iniciales",
            category = "Matemáticas",
            description = "Operaciones básicas, fracciones visuales, geometría y problemas de la vida diaria.",
            keyTopics = listOf("Multiplicación y división", "Fracciones con dibujos", "Perímetros y áreas", "Problemas de lógica"),
            specialToolType = "calculator"
        ),
        SubjectCatalogModule(
            id = "pri_esp",
            level = "Primaria",
            name = "Español y Comprensión",
            category = "Humanidades",
            description = "Lectura comprensiva, ortografía práctica, cuentos y redacción clara.",
            keyTopics = listOf("Acentuación y signos", "Ideas principales", "Fábulas y cuentos", "Exposición oral"),
            specialToolType = "tutor"
        ),
        SubjectCatalogModule(
            id = "pri_nat",
            level = "Primaria",
            name = "Ciencias Naturales",
            category = "Ciencias",
            description = "Ecosistemas, cuerpo humano, ciclo del agua y experimentos seguros en casa.",
            keyTopics = listOf("Cuerpo humano", "Plantas y fotosíntesis", "Estados de la materia", "Cuidado ambiental"),
            specialToolType = "duel"
        ),
        SubjectCatalogModule(
            id = "pri_art",
            level = "Primaria",
            name = "Artes y Creatividad",
            category = "Artes",
            description = "Círculo cromático, origami, ritmo musical y dibujo paso a paso.",
            keyTopics = listOf("Colores primarios y mezclas", "Dibujo con figuras", "Ritmo con palmas", "Manualidades recicladas"),
            specialToolType = "arts"
        ),

        // --- SECUNDARIA ---
        SubjectCatalogModule(
            id = "sec_math",
            level = "Secundaria",
            name = "Álgebra y Geometría",
            category = "Matemáticas",
            description = "Ecuaciones lineales y cuadráticas, teorema de Pitágoras, proporciones y gráficas.",
            keyTopics = listOf("Despeje de ecuaciones", "Sistemas 2x2", "Teorema de Pitágoras", "Funciones lineales"),
            specialToolType = "calculator"
        ),
        SubjectCatalogModule(
            id = "sec_fis",
            level = "Secundaria",
            name = "Física y Química",
            category = "Ciencias",
            description = "Movimiento rectilíneo, leyes de Newton, tabla periódica y balanceo químico.",
            keyTopics = listOf("Velocidad y aceleración", "Fuerza y masa", "Enlaces químicos", "Balanceo por tanteo"),
            specialToolType = "calculator"
        ),
        SubjectCatalogModule(
            id = "sec_hist",
            level = "Secundaria",
            name = "Historia y Formación Cívica",
            category = "Humanidades",
            description = "Líneas del tiempo, civilizaciones antiguas, historia moderna y ciudadanía activa.",
            keyTopics = listOf("Líneas del tiempo", "Revolución Industrial", "Historia nacional", "Derechos y ética"),
            specialToolType = "duel"
        ),
        SubjectCatalogModule(
            id = "sec_art",
            level = "Secundaria",
            name = "Artes Visuales y Música",
            category = "Artes",
            description = "Perspectiva de un punto, claroscuro, armonía de color y creación de proyectos.",
            keyTopics = listOf("Perspectiva básica", "Sombras y volumen", "Composición musical", "Guion escénico"),
            specialToolType = "arts"
        ),
        SubjectCatalogModule(
            id = "sec_ing",
            level = "Secundaria",
            name = "Inglés Práctico",
            category = "Humanidades",
            description = "Tiempos verbales esenciales, vocabulario escolar y conversación guiada.",
            keyTopics = listOf("Present vs Past Simple", "Future with going to", "Reading comprehension", "Pronunciación"),
            specialToolType = "tutor"
        ),

        // --- PREPARATORIA ---
        SubjectCatalogModule(
            id = "prep_calc",
            level = "Preparatoria",
            name = "Trigonometría y Precálculo",
            category = "Matemáticas",
            description = "Identidades trigonométricas, cónicas, logaritmos, límites y derivadas iniciales.",
            keyTopics = listOf("Seno, coseno y tangente", "Logaritmos", "Límites algebraicos", "Derivada por regla"),
            specialToolType = "calculator"
        ),
        SubjectCatalogModule(
            id = "prep_chem",
            level = "Preparatoria",
            name = "Química Orgánica y Bioquímica",
            category = "Ciencias",
            description = "Estequiometría avanzada, pH, hidrocarburos y metabolismo celular.",
            keyTopics = listOf("Moles y molaridad", "Ácidos y bases (pH)", "Grupos funcionales", "Ciclo de Krebs"),
            specialToolType = "calculator"
        ),
        SubjectCatalogModule(
            id = "prep_lit",
            level = "Preparatoria",
            name = "Literatura y Estética del Arte",
            category = "Artes",
            description = "Análisis literario, corrientes artísticas (Renacimiento, Vanguardias) y ensayo crítico.",
            keyTopics = listOf("Figuras retóricas", "Vanguardias artísticas", "Ensayo argumentativo", "Apreciación cinematográfica"),
            specialToolType = "arts"
        ),
        SubjectCatalogModule(
            id = "prep_prog",
            level = "Preparatoria",
            name = "Informática y Pensamiento Lógico",
            category = "Tecnología",
            description = "Pseudocódigo, diagramas de flujo, Python básico y lógica booleana.",
            keyTopics = listOf("Condicionales y ciclos", "Estructuras de control", "Tablas de verdad", "Complejidad básica"),
            specialToolType = "duel"
        ),

        // --- UNIVERSIDAD / CARRERA ---
        SubjectCatalogModule(
            id = "uni_calc",
            level = "Universidad",
            name = "Cálculo Vectorial y Ecuaciones Dif.",
            category = "Matemáticas",
            description = "Integrales múltiples, álgebra lineal, matrices y ecuaciones diferenciales ordinarias.",
            keyTopics = listOf("Eigenvalores y matrices", "Integrales por partes", "EDOs de 1er y 2do orden", "Gradiente y divergencia"),
            specialToolType = "calculator"
        ),
        SubjectCatalogModule(
            id = "uni_cs",
            level = "Universidad",
            name = "Ingeniería de Software e IA",
            category = "Tecnología",
            description = "Estructuras de datos, arquitectura limpia, bases de datos y aprendizaje automático.",
            keyTopics = listOf("Árboles y grafos", "Notación Big-O", "Normalización SQL/NoSQL", "Redes neuronales"),
            specialToolType = "duel"
        ),
        SubjectCatalogModule(
            id = "uni_med",
            level = "Universidad",
            name = "Anatomía, Fisiología y Salud",
            category = "Ciencias",
            description = "Sistemas orgánicos, fisiopatología, farmacología básica y casos clínicos.",
            keyTopics = listOf("Sistema cardiovascular", "Neuroanatomía", "Homeostasis renal", "Casos clínicos"),
            specialToolType = "tutor"
        ),
        SubjectCatalogModule(
            id = "uni_arch",
            level = "Universidad",
            name = "Diseño, Arquitectura y Bellas Artes",
            category = "Artes",
            description = "Composición espacial, teoría del diseño, portafolio creativo y crítica estética.",
            keyTopics = listOf("Proporción áurea y retícula", "Psicología del color", "Materialidad y luz", "Crítica de proyectos"),
            specialToolType = "arts"
        ),
        SubjectCatalogModule(
            id = "uni_econ",
            level = "Universidad",
            name = "Economía, Finanzas y Derecho",
            category = "Humanidades",
            description = "Micro/macroeconomía, valuación financiera, argumentación jurídica y ética profesional.",
            keyTopics = listOf("Oferta y demanda", "Valor presente neto", "Teoría constitucional", "Casos prácticos"),
            specialToolType = "tutor"
        )
    )
}
