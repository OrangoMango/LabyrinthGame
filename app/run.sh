# Change the next line with your javafx/lib path
export FX_PATH=/usr/share/openjfx/lib
cd src
echo "Compiling..."
rm -r ../bin
javac --module-path $FX_PATH --add-modules javafx.controls -d ../bin com/orangomango/labyrinth/LabyrinthMain.java
cd ../bin
echo "Executing..."
java --module-path $FX_PATH --add-modules javafx.controls com.orangomango.labyrinth.LabyrinthMain $1
