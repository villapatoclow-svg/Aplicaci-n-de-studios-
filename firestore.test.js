const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function buildProfile(uid) {
  const now = new Date(Date.now() - 1000);
  return {
    userId: uid,
    displayName: "Ana Estudiante",
    academicLevel: "Secundaria",
    sessionMode: "gamification",
    themeAccent: "indigo",
    petName: "Bubo",
    petLevel: 1,
    petXp: 0,
    streakDays: 1,
    studyCoins: 50,
    ticketsRemaining: 15,
    lastTicketDate: "2026-11-14",
    hasLifetimePass: false,
    hasMonthlyAgentSub: false,
    hasProtonExtra25: false,
    schoolName: "Secundaria Técnica 14",
    schoolNews: "Semana de feria de ciencias y evaluación parcial.",
    studyGroupCode: "",
    ageYears: 18,
    degreeOrMaster: "Ingeniería en Computación",
    petSpecies: "autosauro",
    petAccessory: "ninguno",
    petColorAccent: "indigo",
    weatherSeasonOverride: "auto",
    unlockedBadges: ["inicio"],
    createdAt: now,
    updatedAt: now,
  };
}

function buildSmartTask(uid) {
  const now = new Date(Date.now() - 1000);
  return {
    userId: uid,
    title: "Ecuaciones cuadráticas",
    subject: "Matemáticas",
    description: "Resolver 5 ejercicios paso a paso",
    procedureSteps: ["Identificar a, b, c", "Aplicar fórmula general"],
    completed: false,
    xpReward: 40,
    dueDateLabel: "Hoy",
    createdAt: now,
    updatedAt: now,
  };
}

function buildWorkshopItem(authorId) {
  const now = new Date(Date.now() - 1000);
  return {
    authorId,
    authorName: "Ana",
    title: "Plan intensivo de Física",
    category: "plan",
    subject: "Física",
    academicLevel: "Secundaria",
    description: "Cinemática y dinámica en 4 sesiones.",
    steps: ["Vectores", "MRU", "MRUA"],
    visibility: "public",
    downloadsCount: 0,
    likesCount: 0,
    createdAt: now,
    updatedAt: now,
  };
}

test("Unauthenticated user cannot read or write profiles or tasks", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("smart_tasks").get());
  await assertFails(unauthDb.collection("workshop_items").where("visibility", "==", "public").get());
});

test("Owner can create and update their own profile, cross-user access denied", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set(buildProfile(ALICE_UID))
  );

  // Cross-user read/write blocked
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({ petXp: 999, updatedAt: new Date(Date.now() - 500) })
  );

  // Shadow field injection blocked
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({ isAdmin: true, updatedAt: new Date(Date.now() - 500) })
  );

  // Valid partial update succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      sessionMode: "numeric",
      petXp: 60,
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Owner can create, list, update, and delete smart tasks; cross-user blocked", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const taskRef = aliceDb.collection("users").doc(ALICE_UID).collection("smart_tasks").doc("task_1");

  await assertSucceeds(taskRef.set(buildSmartTask(ALICE_UID)));
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("smart_tasks").get());

  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("smart_tasks").get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("smart_tasks").doc("task_1").delete()
  );

  await assertSucceeds(taskRef.update({ completed: true, updatedAt: new Date(Date.now() - 500) }));
  await assertSucceeds(taskRef.delete());
});

test("Workshop items require visibility=='public' filter, support community download/like increments, block tampering", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  const itemRef = aliceDb.collection("workshop_items").doc("item_1");
  await assertSucceeds(itemRef.set(buildWorkshopItem(ALICE_UID)));

  // Unfiltered workshop list fails (rules are not filters)
  await assertFails(bobDb.collection("workshop_items").get());

  // Filtered workshop list succeeds
  await assertSucceeds(
    bobDb.collection("workshop_items").where("visibility", "==", "public").get()
  );

  // Bob can increment downloadsCount by 1
  await assertSucceeds(
    bobDb.collection("workshop_items").doc("item_1").update({
      downloadsCount: 1,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Bob CANNOT edit Alice's title or jump downloadsCount by 100
  await assertFails(
    bobDb.collection("workshop_items").doc("item_1").update({
      title: "Hacked",
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertFails(
    bobDb.collection("workshop_items").doc("item_1").update({
      downloadsCount: 100,
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Owner can manage school_teachers with valid schema, cross-user blocked", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 1000);
  const teacherRef = aliceDb.collection("users").doc(ALICE_UID).collection("school_teachers").doc("teach_1");

  await assertSucceeds(
    teacherRef.set({
      userId: ALICE_UID,
      name: "Prof. Morales",
      subject: "Matemáticas",
      focusNote: "Examen parcial el miércoles",
      howToHelp: "Llevar guía de fórmulas resueltas para apoyar al grupo.",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("school_teachers").get());
  await assertSucceeds(
    teacherRef.update({
      howToHelp: "Preparar 2 ejercicios modelo para el pizarrón.",
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertSucceeds(teacherRef.delete());
});

test("Owner can manage finance_entries for Plan Alto; cross-user and invalid types blocked", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 1000);
  const finRef = aliceDb.collection("users").doc(ALICE_UID).collection("finance_entries").doc("fin_1");

  await assertSucceeds(
    finRef.set({
      userId: ALICE_UID,
      title: "Beca Universitaria",
      amount: 4500.0,
      entryType: "ingreso",
      category: "Beca",
      note: "Pago mensual de posgrado",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("finance_entries").get());
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("finance_entries").doc("fin_bad").set({
      userId: ALICE_UID,
      title: "Inválido",
      amount: -50,
      entryType: "crypto_scam",
      category: "Otro",
      note: "",
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertSucceeds(finRef.delete());
});

test("Verified villapatoclow@gmail.com admin can list users, inspect tasks, grant passes, and delete items; unverified or other users blocked", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const spoofedAdminDb = testEnv
    .authenticatedContext("spoof_uid", {
      email: "villapatoclow@gmail.com",
      email_verified: false,
    })
    .firestore();
  const adminDb = testEnv
    .authenticatedContext("admin_uid", {
      email: "villapatoclow@gmail.com",
      email_verified: true,
    })
    .firestore();

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set(buildProfile(ALICE_UID)));
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("smart_tasks").doc("t1").set(buildSmartTask(ALICE_UID))
  );
  await assertSucceeds(aliceDb.collection("workshop_items").doc("ws1").set(buildWorkshopItem(ALICE_UID)));

  // Regular user & unverified email cannot list /users
  await assertFails(bobDb.collection("users").get());
  await assertFails(spoofedAdminDb.collection("users").get());
  await assertFails(spoofedAdminDb.collection("workshop_items").doc("ws1").delete());

  // Verified admin can list /users, inspect Alice's tasks, update Alice's pass, delete task, workshop item, and user doc
  await assertSucceeds(adminDb.collection("users").get());
  await assertSucceeds(adminDb.collection("users").doc(ALICE_UID).collection("smart_tasks").get());
  await assertSucceeds(
    adminDb.collection("users").doc(ALICE_UID).update({
      hasProtonExtra25: true,
      hasLifetimePass: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertSucceeds(
    adminDb.collection("users").doc(ALICE_UID).collection("smart_tasks").doc("t1").delete()
  );
  await assertSucceeds(adminDb.collection("workshop_items").doc("ws1").delete());
  await assertSucceeds(adminDb.collection("users").doc(ALICE_UID).delete());
});


