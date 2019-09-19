import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * The main application for user interaction.
 * <p/>
 * Is responsible for updating all settings and listing mods with their corresponding information.
 * The main application also distributes tasks to other classes, which should then execute them
 * on another {@link Thread}, to ensure that the application will continue to run smoothly and not
 * stop user interaction while processing!
 *
 * @see javafx.application.Application
 */
public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("application/main.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Runs the main application with the specified arguments.
     * @param args the arguments for the application
     */
    public static void main(String[] args) {
        launch(args);
    }
}
