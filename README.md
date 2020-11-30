# BeeSafe
BeeSafe is a P2P Android Application which detects and notifies you about crowds in your local area. It scans continously in background for devices using Bluetooth and uploads data to a real-time database. This data is later shown on a heatmap which approximates the number of people located in a certain area.

#How it works?
This Android Application is made for the course Software Engineering AA 20/21 Ca' Foscari University.It shows the crowds nearby your location and represent them using HeatMap.It has a background service which scans the current location continuously by a tracing algorithm.The scan gets uploaded to a realtime database. A location is represented using a GeoHash for privacy purposes and efficiency. No personal data are stored on servers.

The user has the ability to save a favorite location and get notified every time the location gets crowded. To add a favorite place, the user should long click on a location on the map. Then, a popup will be displayed where can enter a custom name for that location and enable or disable the notifications. To see information about a saved place, the users clicks on the pinpoint created by the app, where a new dialog shows information about this place like the density, last update and an approximation made by the tracing algorithm. The user has tha ability to remove a favorite place from saved.

 Data on the realtime database gets refreshed often to improve the approximation, and moved to a new database, which holds one-week data. These data is used byt the prevision algorithm to predict a crowd place.
 Predication is not yet supported, and will be available in the Beta version.

# Group Details
    Name: Buffer Overflow
    Membri:
        Hernest Serani  @electronixxx
        Stefano Cappon  @876895
        Enrico Baldasso @Enrico874885
