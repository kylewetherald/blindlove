BlindLove
====================

To run/test the app
--------------------

* On windows use activator.bat from command line. On *nix just use ./activator
* Once in the activator console, enter "run" to run the app. Check it out in your browser at localhost:9000
* To run tests, press Ctrl+D (when the app is running) to end the app execution. Then from the activator console, enter "test"

Important notes
-----------------
* Sources are primarily located in the app/ directory. There is some relevant configuration in conf/
* The database is automatically constructed in the db directory. During development you may need to delete this directory and re-run the application to repopulate the test database
* app/Global.java contains the code that creates test data
* app/views has the html views
* Static files can be placed in the /public/* directory and will be accessible at the /assets/* URL

Play2 Documentation
--------------------
* [Anatomy of a Play app](http://www.playframework.com/documentation/2.2.x/Anatomy)
* [Using the Play console](http://www.playframework.com/documentation/2.2.x/PlayConsole)
* [Play! for Java developers](http://www.playframework.com/documentation/2.2.x/JavaHome) is the general reference you will use regularly
* [eBeans](http://www.playframework.com/documentation/2.2.x/JavaForms), the database system
* [Info on the templating system](http://www.playframework.com/documentation/2.2.x/JavaTemplates)
* [Info on building web forms](http://www.playframework.com/documentation/2.2.x/JavaForms)
* Take a look at the "Computer Database" sample application. Look in your play distribution folder in the samples\java\computer-database directory.