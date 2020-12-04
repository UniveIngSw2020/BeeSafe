import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';
admin.initializeApp();

// Start writing Firebase Functions
// https://firebase.google.com/docs/functions/typescript
//firebase deploy --only "functions:helloWorld"
//firebase deploy
export const helloWorld = functions.https.onRequest((request, response) => {
//   functions.logger.info("Hello logs!", {structuredData: true});
  response.send("Hello from Firebase!");
});

/*
export const deleteOldDatas = functions.firestore
  .document("beesafe-bde99")
  .onUpdate((change, context) => {
    // Get an object representing the document
    const updatedPost = change.after.data() as any;

    // ...or the previous value before this update
    const oldPost = change.before.data() as any;

    const oldDatas: string[] = oldPost.images;
    const newDatas: string[] = updatedPost.images;

    const deletedDatas = oldDatas.filter(oldDatas => {
      return !newDatas.some(newDatas => newDatas === oldDatas);
    });

    const bucket = firebase.storage().bucket();

    const datasRemovePromises = deletedDatas.map((dataPath: string) => {
      return bucket.file(dataPath).delete();
    });

    return Promise.all(datasRemovePromises);
  });
  */