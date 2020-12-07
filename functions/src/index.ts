import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';

admin.initializeApp();
admin.database();

//firebase deploy --only "functions:getData"
//firebase serve --only "functions:getData"
// getData from database

export const getData = functions.https.onRequest(async (request, response) => {
  const db = admin.database();
  const ref = db.ref();
  ref.on("value", function(snapshot:any) {
    const allDb = snapshot.val();
    for(const k in allDb){
      for(const i in allDb[k]){
        console.log(allDb[k][i].lastSeen);
      }
    }
    response.send(allDb);
  }, function (error) {
    console.log("The read failed: " + error);
  });
  
})



/*
// Start writing Firebase Functions
// https://firebase.google.com/docs/functions/typescript
//firebase deploy --only "functions:helloWorld"
//firebase deploy
export const helloWorld = functions.https.onRequest((request, response) => {
//   functions.logger.info("Hello logs!", {structuredData: true});
  response.send("Hello from Firebase!");
});
*/

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