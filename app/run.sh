export FX_PATH=/usr/share/openjfx/lib
cd src
javac -d ../bin com/orangomango/labyrinth/LabyrinthMain.java
#javac --module-path $FX_PATH --add-modules javafx.controls -d ../bin com/orangomango/labyrinth/LabyrinthMain.java
cd ../bin
#java --module-path $FX_PATH --add-modules javafx.controls com.orangomango.labyrinth.LabyrinthMain
java com.orangomango.labyrinth.LabyrinthMain
