export FX_PATH=/usr/share/openjfx/lib
#export FX_PATH=/home/runner/LabyrinthGame-1/javafx-sdk-11.0.2/lib
cd src
javac --module-path $FX_PATH --add-modules javafx.controls -d ../bin com/orangomango/labyrinth/LabyrinthMain.java
cd ../bin
java --module-path $FX_PATH --add-modules javafx.controls com.orangomango.labyrinth.LabyrinthMain
