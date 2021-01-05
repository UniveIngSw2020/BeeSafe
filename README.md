# BeeSafe
BeeSafe is a P2P Android Application which detects and notifies you about crowds in your local area. It scans continously in background for devices using Bluetooth and uploads data to a real-time database. This data is later shown on a heatmap which approximates the number of people located in a certain area.

# Author Contributions
* Hernest Serani ([electronixxx](https://github.com/electronixxx)) - Android App Development & Firebase Database
* Enrico Baldasso ([Enrico874885](https://github.com/Enrico874885)) - Firebase Functions
* Stefano Cappon ([876895](https://github.com/876895)) - Database & Testing

## Screenshots
##### App Screenshots

<div style="display:inline-block; ">
    <img src="/Screenshots/map.jpg#right" alt="drawing" width="230"/>
    <img src="/Screenshots/Add_place.jpg" alt="drawing" width="247"/>
    <img src="/Screenshots/info.jpg" alt="drawing" width="230"/>
</div>

<div style= "display:inline-block;">
    <img src="/Screenshots/home.jpg" alt="drawing" width="230"/>
    <img src="/Screenshots/no_data.jpg" alt="drawing" width="238"/>
    <img src="/Screenshots/help.jpg" alt="drawing" width="239"/>
</div>


#### Notifications Screenshots
<div style= "display:inline-block;">
    <img src="/Screenshots/fav.jpg" alt="drawing" width="250"/>
<img src="/Screenshots/crowd_current_location_notification.jpg" alt="drawing" width="240"/>
<img src="/Screenshots/gp_BL_not_active.jpg" alt="drawing" width="240"/>
</div>


# How it works?
BeeSafe is made for the course Software Engineering AA 20/21 Ca' Foscari University. It shows the crowds nearby your location and represent them using a HeatMap. It has a background service which scans the current location continuously by a tracing algorithm. The scan gets uploaded to a realtime database. A location is represented using a GeoHash for privacy purposes and efficiency. No personal data which can identify you are stored on the server.

The user has the possibility to save a favorite location and get notified every time the location gets crowded. To add a favorite place, the user should long click on a location on the map. Then, a popup will be displayed where can enter a custom name for that location and enable or disable the notifications. To see information about a saved place, the users clicks on the pinpoint generated by the app, where a new dialog shows information about this place like the density, last update and an approximation made by the tracing algorithm. The user can remove a favorite place from saved.

 Data on the realtime database gets refreshed often to improve the approximation, and moved to a new database, which holds one-week data. These data is used byt the prevision algorithm to predict a crowd place.
 Predication is not yet supported, and will be as a feature, planned for newer releases.

