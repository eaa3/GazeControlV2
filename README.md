GazeControlV2
=============

This is a Java Simulation of three Gaze Control Models for manipulation: Unc, RU and RUG.


# How to run it
## Running in eclipse

1. You can simply run it using Eclipse

*However, you may need to change the simulation parameters by changing the code directly or modifying the "Run Configurations" of the project adding program arguments detailed below.*

## Exporting and running as .jar executable

1. Import project in Eclipse IDE.
2. Export the project as a java runnable file (.jar).
3. Put the *.jar file inside the project directory (.e.g. /Users/someUserFolder/workspace/GazeControlV2/ ).
4. Now you can choose two ways of running it 

* First way: using the terminal, go to the folder where you put .jar file and run the following command

  `source experiment.sh`
*This will run all the simulations sequentially (have a look at the experiment.sh for more details)*

* Second way: using the terminal, go to the folder where you put .jar file and run the following command

  `java -jar exp.jar <InitialGraspingThreshold> <InitialFOV> <TrialTime (ms)> <GazeControlModel> <ExperimentType>`

  *e.g.* `java -jar exp.jar 1 15 60000 RUG GRSP`

Where,
* exp.jar is the name of the exported .jar file
* InitialGraspingThreshold: Initial grasping threshold (usually set to 1)
* InitialFOV: Initial Field of View -FOV (e.g. 15)
* TrialTime (ms): Trial time in miliseconds.
* GazeControlModel: Should be one of the follwing: `UNC`, `RU` or `RUG`
* ExperimentType: There are two experiments, the grasping experiment and the FOV experiment. So, you should select one of the following: `GRSP` or `FOV`


