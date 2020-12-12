import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';

//Push in Productions : firebase deploy
//Test localhost :
//    firebase deploy --only "functions:cron"
//    firebase serve --only "functions:cron"

admin.initializeApp();
admin.database();
// https://firebase.google.com/docs/functions/typescript
//Elimina dati più vecchi di un ora hours //
export const cronData = functions.pubsub.schedule('every 2 hours ').onRun((context) => {
  const db = admin.database();
  const ref = db.ref();//ref() take all database
  
  ref.on("value", function(snapshot:any) {
    const allDb = snapshot.val();
    const toDelete :any = [];
    function elemToDelete() {
      for(const k in allDb) {
        for(const i in allDb[k]) {
          try{
            const time:String = allDb[k][i].lastSeen;//'2020-11-15T11:25:037'
            if(time !== undefined){
              if(!isValidDate(time)){
                if(toDelete[k] === undefined){toDelete[k] = [];}
                toDelete[k].push(i);
              }
            }else{
              if(toDelete[k] === undefined){toDelete[k] = [];}
              toDelete[k].push(i);
              console.log('Invalid time undefined');
            }
          }catch(e){
            if(toDelete[k] === undefined){toDelete[k] = [];}
            toDelete[k].push(i);
            console.log('Invalid time :', allDb[k][i], 'Error :', e);
            //response.send(e);
          }
        }
      }
      return toDelete;
    }
    //console.log(toDelete);
    elemToDelete();
    /* ------------------- DELETE ------------------- */
     
    const FieldValue = admin.firestore.FieldValue;
    for(const k in toDelete) {
      const dataRef = db.refFromURL('https://beesafe-bde99.firebaseio.com/'+k.toString());
      //console.log('Riferimento : '+dataRef);
      for(const i in toDelete[k]) {
        let info;
        try {
          info = dataRef.update({
            [toDelete[k][i]]: FieldValue.delete(),
          });
          
        } catch (error) {
          console.log('Not remove obj in path : '+k+'.'+toDelete[k][i]+' Info : '+info);
          return false;
        }
        
      }
    }
    /*  ---------------------------------------------------- */
    /* ------------------- UPDATE ------------------- 
      for(const k in toDelete) {
        for(const i in toDelete[k]) {
          try {
            const dataRef = db.refFromURL('https://beesafe-bde99.firebaseio.com/'+k.toString()+'/'+toDelete[k][i]);
            console.log('Riferimento : '+dataRef);
            const date: Date = new Date();
            const strDate = JSON.stringify(date);
            const newData = strDate.substring(1, strDate.length-1);
            const info = dataRef.update({
              lastSeen : newData,
            });
            console.log(info);
          } catch (error) {
            console.log('Not update lastSeen obj in path : '+k+'.'+toDelete[k][i]);
            //response.send(error);
          }
        }
      }
       ---------------------------------------------------- */
    //response.send(toDelete);
    return true;
  }, function (error) {
    console.log("The read failed : " + error);
    //response.send(error);
    return false;
  });
  
})

function isValidDate(time : String) : Boolean {//time : '2020-11-15T11:25:037'
  const delayHour  = 1;
  const delay = (3600000)*(delayHour-1);//1 hour in millisec, -1 cause ita time
  const date: Date = new Date();
  const boundTime: Date = new Date((date.getTime() - delay));
  
  let strBTime = JSON.stringify(boundTime);
  strBTime = strBTime.split('-')[2];
  const year : number = +(time.split('-')[0]);
  const boundY : number = boundTime.getFullYear();
  const mouth : number = +(time.split('-')[1]);
  const boundMou : number = boundTime.getMonth()+1;
  const day : number = +(time.split('-')[2].substring(0, time.split('-')[2].search('T')));
  const boundD : number = Number(strBTime.substring(0, strBTime.search('T')));
  const hour : number = +(time.substring(time.search('T')+1, time.search(':')));
  const boundH : number = boundTime.getHours();
  const min : number = +(time.split(':')[1]);
  const boundMin : number = boundTime.getMinutes();

  //console.log('Y['+year+' - '+boundY+'] M['+mouth+' - '+boundMou+'] D['+day+' - '+boundD+'] H['+hour+' - '+boundH+'] Min['+min+' - '+boundMin+']');

  const isNotValid = (year<boundY
    || (year===boundY && mouth<boundMou)
    || (year===boundY && mouth===boundMou && day<boundD)
    || (year===boundY && mouth===boundMou && day===boundD && hour<boundH)
    || (year===boundY && mouth===boundMou && day===boundD && hour===boundH && min<=boundMin));

  return !isNotValid;
}
