const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ADMIN_UID = "admin_uid_001";
const ADMIN_EMAIL = "aistudio.gamer.2@gmail.com";
const ALICE_UID = "alice_uid_123";
const BOB_UID = "bob_uid_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validCatalogPayload(uid, overrides = {}) {
  const now = new Date(Date.now() - 2000);
  return {
    title: "Mindustry",
    titleAr: "مايندستري",
    itemType: "GAME",
    category: "STRATEGY",
    developer: "Anuken",
    summary: "Factory automation and sci-fi tower defense.",
    summaryAr: "لعبة استراتيجية لبناء المصانع.",
    description: "Build supply chains and defend your core.",
    descriptionAr: "ابنِ خطوط الإمداد ودافع عن قاعدتك.",
    version: "146.0",
    fileSize: "78 MB",
    downloadUrl: "https://github.com/Anuken/Mindustry/releases",
    coverImageUrl: "preset:nova_strike",
    screenshotUrls: ["preset:nova_strike", "preset:cyber_drift"],
    rating: 4.9,
    downloadsCount: 0,
    featured: true,
    published: true,
    createdBy: uid,
    createdAt: now,
    updatedAt: now,
    ...overrides,
  };
}

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

// --- 1. UNAUTHENTICATED ACCESS DENIAL ---
test("Unauthenticated users cannot read or write any collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("catalog_items").where("published", "==", true).get()
  );
  await assertFails(
    unauthDb.collection("catalog_items").doc("item_1").set(validCatalogPayload(ADMIN_UID))
  );
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("favorites").get()
  );
});

// --- 2. ADMIN CATALOG CRUD & EMAIL SPOOFING PROTECTION ---
test("Bootstrapped verified admin can create, update, feature, and delete catalog items", async () => {
  const adminDb = testEnv
    .authenticatedContext(ADMIN_UID, {
      email: ADMIN_EMAIL,
      email_verified: true,
    })
    .firestore();

  const ref = adminDb.collection("catalog_items").doc("mindustry_1");
  await assertSucceeds(ref.set(validCatalogPayload(ADMIN_UID)));

  // Partial update mirroring StoreRepository.updateCatalogItem (preserves createdBy & createdAt)
  await assertSucceeds(
    ref.update({
      title: "Mindustry Updated",
      version: "147.0",
      featured: false,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Admin queries: published == true AND createdBy == ADMIN_UID
  await assertSucceeds(
    adminDb.collection("catalog_items").where("published", "==", true).get()
  );
  await assertSucceeds(
    adminDb.collection("catalog_items").where("createdBy", "==", ADMIN_UID).get()
  );

  await assertSucceeds(ref.delete());
});

test("Email spoofing attack with unverified email is rejected", async () => {
  const spoofDb = testEnv
    .authenticatedContext("spoof_uid", {
      email: ADMIN_EMAIL,
      email_verified: false,
    })
    .firestore();

  await assertFails(
    spoofDb
      .collection("catalog_items")
      .doc("spoof_item")
      .set(validCatalogPayload("spoof_uid"))
  );
});

test("Non-admin user cannot create, edit fields, or delete catalog items", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx
      .firestore()
      .collection("catalog_items")
      .doc("mindustry_1")
      .set(validCatalogPayload(ADMIN_UID));
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();

  // Cannot create
  await assertFails(
    aliceDb
      .collection("catalog_items")
      .doc("alice_hack")
      .set(validCatalogPayload(ALICE_UID))
  );

  // Cannot edit title or downloadUrl
  await assertFails(
    aliceDb.collection("catalog_items").doc("mindustry_1").update({
      title: "Hijacked",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Cannot delete
  await assertFails(
    aliceDb.collection("catalog_items").doc("mindustry_1").delete()
  );
});

// --- 3. CATALOG VALIDATION, IMMUTABILITY & DOWNLOAD INCREMENT ACTION ---
test("Authenticated user can list published items and increment downloadsCount by +1 only", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx
      .firestore()
      .collection("catalog_items")
      .doc("game_1")
      .set(validCatalogPayload(ADMIN_UID, { downloadsCount: 10 }));
    await ctx
      .firestore()
      .collection("catalog_items")
      .doc("draft_1")
      .set(validCatalogPayload(ADMIN_UID, { published: false }));
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();

  // Published query matches Kotlin StoreRepository.observePublishedCatalog
  await assertSucceeds(
    aliceDb.collection("catalog_items").where("published", "==", true).get()
  );

  // Unfiltered list query MUST fail (Query Trust Test)
  await assertFails(aliceDb.collection("catalog_items").get());

  // Reading unpublished draft as non-admin fails
  await assertFails(aliceDb.collection("catalog_items").doc("draft_1").get());

  // Valid +1 download increment succeeds
  await assertSucceeds(
    aliceDb.collection("catalog_items").doc("game_1").update({
      downloadsCount: 11,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Jumping +50 downloads fails
  await assertFails(
    aliceDb.collection("catalog_items").doc("game_1").update({
      downloadsCount: 61,
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Rejects ghost fields, @DocumentId id key, future timestamps, and invalid URLs", async () => {
  const adminDb = testEnv
    .authenticatedContext(ADMIN_UID, {
      email: ADMIN_EMAIL,
      email_verified: true,
    })
    .firestore();

  // Ghost field / @DocumentId id key rejected
  await assertFails(
    adminDb
      .collection("catalog_items")
      .doc("bad_id_field")
      .set(validCatalogPayload(ADMIN_UID, { id: "bad_id_field" }))
  );

  // Invalid download URL scheme rejected
  await assertFails(
    adminDb
      .collection("catalog_items")
      .doc("bad_url")
      .set(validCatalogPayload(ADMIN_UID, { downloadUrl: "javascript:alert(1)" }))
  );

  // Future timestamp rejected
  await assertFails(
    adminDb
      .collection("catalog_items")
      .doc("future_ts")
      .set(
        validCatalogPayload(ADMIN_UID, {
          createdAt: new Date(Date.now() + 60000),
          updatedAt: new Date(Date.now() + 60000),
        })
      )
  );
});

// --- 4. USER PROFILE PII ISOLATION, FAVORITES & DOWNLOAD LOGS ---
test("User profiles are strictly owner-isolated and reject role self-assignment", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 2000);

  // Alice creates her own profile
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice Gamer",
      email: "alice@example.com",
      preferredLanguage: "ar",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Alice updates language preference
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      preferredLanguage: "en",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Role escalation shadow field rejected
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      role: "admin",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Bob cannot read or list Alice's PII
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").get());
});

test("User favorites and downloads are owner-isolated and require existing catalog items", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx
      .firestore()
      .collection("catalog_items")
      .doc("game_1")
      .set(validCatalogPayload(ADMIN_UID));
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 1500);

  // Alice lists empty subcollections cleanly
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("favorites").get()
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("downloads").get()
  );

  // Alice saves existing item as favorite
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("favorites")
      .doc("game_1")
      .set({
        itemId: "game_1",
        itemTitle: "Mindustry",
        itemType: "GAME",
        createdAt: now,
      })
  );

  // Orphaned favorite referencing non-existent catalog item fails
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("favorites")
      .doc("missing_item")
      .set({
        itemId: "missing_item",
        itemTitle: "Ghost",
        itemType: "GAME",
        createdAt: now,
      })
  );

  // Alice logs a download
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("downloads")
      .doc("dl_1")
      .set({
        itemId: "game_1",
        itemTitle: "Mindustry",
        itemType: "GAME",
        version: "146.0",
        downloadUrl: "https://github.com/Anuken/Mindustry/releases",
        createdAt: now,
      })
  );

  // Bob cannot read or delete Alice's favorites or downloads
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("favorites").get()
  );
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("favorites")
      .doc("game_1")
      .delete()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("downloads").get()
  );
});
