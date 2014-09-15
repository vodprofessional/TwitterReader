TwitterReader
==================

The application requires at least Java 1.7 to run. Make sure you have an installed JDK with appropriate version.

You can compile the application after installing sbt (http://www.scala-sbt.org/release/docs/Getting-Started/Setup.html).
Start the

    sbt compile stage

command in the application root directory using the command line. Configuration files can be found in the 
target/universal/stage/conf/ directory. The application.conf is the production configuration file. You have to configure 
the database connection, the twitter OAuth credentials at least to start up the services.

Optionally you can customize the user and password for the web interface.

Preparing the database
------------------------

The v2 iteration of the services does not support migration from the Proof-of-Concept application (v1) regarding the 
database schema. Please start with a clean and empty database.

Running the services
----------------------

You can then run the application with web-only mode with the following command:

    target/universal/stage/bin/socialexplorer <portnumber>

You can run only the twitter firehose processor without the web service with the following command:

    target/universal/stage/bin/socialexplorer -worker
  
If you wish to run both the web service and the Twitter firehose services use the following command:

    target/universal/stage/bin/socialexplorer <portnumber> -worker

The separation of services is there because Heroku (and other cloud PaaS) allow running worker processes and web 
processes separately. If you plan to run a worker and web node in parallel configure them as follows by modifying the
file name "Procfile" in the root of the source code:

    web: target/universal/stage/bin/socialexplorer $PORT
    worker: target/universal/stage/bin/socialexplorer -worker
    
And then scale the nodes up on the PaaS console(i.e. adding 1 server to web and 1 to worker). 

If you want to run only one node (i.e. the web on Heroku), then use the following Procfile:

    web: target/universal/stage/bin/socialexplorer $PORT -worker

Currently there is no point in running multiple workers or web interfaces. Since only one client is allowed to join the
Twitter firehose with the same credentials, it will just cause errors if you start more than one. 

Using more than one web node is also not supported with the current configuration.

Configuring logging and notifications
-----------------------------------------

You can configure logging and notifications in the target/universal/stage/conf/logback.xml file. Examples are included
in the file, commented out. The email sending configuration is implemented by the SMTPAppender. It only triggers an email
if an error happens or any of the services crash. The Pushover notification is implemented the same way, via the 
PushoverAppender section. Please provide your token and user values as the example shows. It also only triggers a 
notification when an error happens or one of the services crash.