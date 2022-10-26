## Running our CorDapp

Normally, you'd interact with a CorDapp via a client or webserver. So we can
focus on our CorDapp, we'll be running it via the node shell instead.

Once you've finished the CorDapp's code, run it with the following steps:

* Build a test network of nodes by opening a terminal window at the root of
  your project and running the following command:

  * Windows:   `gradlew.bat deployNodes`
  * Linux/Mac:     `./gradlew deployNodes`

* Start the nodes by running the following command:

  * Windows:   `build\nodes\runnodes.bat`
  * Linux/Mac: `./build/nodes/runnodes`



* Build and Start the nodes using docker:
  * Windows:   `gradlew.bat prepareDockerNodes`
  * Linux/Mac:     `./gradlew prepareDockerNodes`
  * `ACCEPT_LICENSE=Y | docker-compose -f ./build/nodes/docker-compose.yml up`

