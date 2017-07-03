import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;

public class Main extends Application {
    private ArrayList<Integer> key = new ArrayList<>();
    private SecureRandom random = new SecureRandom();
    private FileChooser chooser;
    private Stage chooserStage;
    private byte[] data = new byte[0];
    private Pane root;
    private TextField fieldEnc,fieldDec,fieldEncRange,fieldKey;
    private Button buttonEnc, buttonChooserEnc,buttonDec,buttonChooserDec,buttonChooserKey;
    private short encRange=128;
    private Alert alert;
    @Override
    public void start(Stage primaryStage) throws Exception {
        root = new Pane();
        fieldEnc = new TextField();
        buttonEnc = new Button("Encrypt");
        buttonChooserEnc = new Button("Choose file");
        buttonDec = new Button("Decrypt");
        fieldDec = new TextField();
        buttonChooserDec = new Button("Choose File");
        fieldEncRange = new TextField();
        fieldKey = new TextField("Path to key");
        buttonChooserKey = new Button("Choose key");
        buttonChooserKey.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                chooser(chooser,chooserStage,fieldKey);
            }
        });
        buttonDec.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (fieldDec.getText().isEmpty()){
                   alertError("file!");
                    return;
                }
                if (fieldKey.getText().isEmpty()||fieldKey.getText().equals("Path to key")) {
                    alertError("key!");
                    return;
                }
                Stage stage = new Stage();
                buttonDec.setDisable(true);
                progress(stage);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        decrypting(fieldDec.getText());
                        buttonDec.setDisable(false);
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                stage.close();
                            }
                        });
                    }
                }).start();
            }
        });
        buttonChooserEnc.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                chooser(chooser,chooserStage,fieldEnc);
            }
        });
        buttonEnc.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (fieldEnc.getText().isEmpty()){
                    alertError("file!");
                    return;
                }
                if (fieldKey.getText().isEmpty()||fieldKey.getText().equals("Path to key")){
                   alertError("key!");
                    return;
                }
                if (fieldEncRange.getText().isEmpty()){
                    encRange=128;
                } else {
                    encRange=Short.parseShort(fieldEnc.getText());
                }
                Stage stage = new Stage();
                buttonEnc.setDisable(true);
                progress(stage);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        encrypting(fieldEnc.getText());
                        buttonEnc.setDisable(false);
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                stage.close();
                            }
                        });
                    }
                }).start();
            }
        });
        buttonChooserDec.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                chooser(chooser,chooserStage,fieldDec);
            }
        });
        fieldEnc.setPrefSize(320, 20);
        fieldEnc.setLayoutX(10);
        fieldEnc.setLayoutY(0);
        buttonChooserEnc.setLayoutX(350);
        buttonChooserEnc.setLayoutY(0);
        buttonEnc.setLayoutX(450);
        buttonEnc.setLayoutY(0);
        fieldDec.setLayoutX(10);
        fieldDec.setLayoutY(40);
        fieldDec.setPrefSize(320,20);
        buttonChooserDec.setLayoutX(350);
        buttonChooserDec.setLayoutY(40);
        buttonDec.setLayoutX(450);
        buttonDec.setLayoutY(40);
        fieldEncRange.setLayoutX(10);
        fieldEncRange.setLayoutY(80);
        fieldEncRange.setPrefSize(120,20);
        fieldKey.setPrefSize(230,20);
        fieldKey.setLayoutX(187);
        fieldKey.setLayoutY(80);
        buttonChooserKey.setLayoutX(427);
        buttonChooserKey.setLayoutY(80);
        root.getChildren().addAll(fieldEnc,buttonChooserEnc,buttonEnc,fieldDec,buttonChooserDec,buttonDec,
                fieldEncRange,fieldKey,buttonChooserKey);
        primaryStage.setTitle("Encipher");
        primaryStage.setScene(new Scene(root, 530, 110));
        primaryStage.setResizable(false);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

    private void encrypting(String path) {
        try {
            data = Files.readAllBytes(new File(path).toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        int x;
        for (int i = 0; i < data.length; i++) {
            x = random.nextInt(encRange) + 1 * (int) new Date().getTime();
            while (x==0){
                x = random.nextInt(encRange) + 1 * (int) new Date().getTime();
            }
            key.add(x);
            data[i] += key.get(i);
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("File encoded");
                alert.setHeaderText("Done!!!");
                alert.showAndWait();
            }
        });
        try (BufferedWriter br = new BufferedWriter(new FileWriter(fieldKey.getText()))) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < key.size(); i++) {
                builder.append(key.get(i) + " ");
            }
            br.write(builder.toString());

        } catch (IOException e) {
            e.printStackTrace();
           alertFError("Failed to write key");
        }
        try (FileOutputStream fos = new FileOutputStream(path + " Encrypted")) {
            fos.write(data, 0, data.length);
        } catch (IOException e) {
            e.printStackTrace();
            alertFError("Failed to write file");
        }
        alertSuccess("File written");
    }

        private void decrypting(String path){
            StringBuilder sb = new StringBuilder();
            String text;
            String[] array;
            try(BufferedReader br = new BufferedReader(new
                    FileReader(fieldKey.getText()))){
                while((text=br.readLine())!=null){
                    sb.append(text);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                alertFError("File not found");
            } catch (IOException e) {
                e.printStackTrace();
               alertFError("Error");
            }
            array=sb.toString().split(" ");
            try {
                data = Files.readAllBytes(new File(path).toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (int i=0;i<data.length;i++){
                data[i]-=Integer.parseInt(array[i]);
            }
        try (FileOutputStream fos = new FileOutputStream(path+" Decrypted")){
            fos.write(data,0,data.length);
        } catch (IOException e) {
        e.printStackTrace();
            alertFError("Failed to write file");
    }
            alertSuccess("File written");
        }

        private void chooser(FileChooser chooser,Stage chooserStage,TextField field){
            if (chooser == null)
                chooser = new FileChooser();
            if (chooserStage == null) {
                chooserStage = new Stage();
            }
            File selectedDirectory = chooser.showOpenDialog(chooserStage);
            if (selectedDirectory != null) {
                field.setText(selectedDirectory.getAbsolutePath());
            }
        }

        private void alertFError(String action){
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Failed :-(");
            alert.setHeaderText(action);
            alert.showAndWait();
        }

        private void alertSuccess(String action){
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success :-)");
                    alert.setHeaderText(action);
                    alert.showAndWait();
                }
            });
        }

        private void alertError(String action){
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("ERROR");
                    alert.setHeaderText("No path to " + action);
                    alert.showAndWait();
                }
            });
        }

        private void progress(Stage stage){
            stage.setTitle("Waiting");
            stage.setResizable(false);
            FlowPane flowPane = new FlowPane();
            ImageView imageView = new ImageView(new Image("progress.gif"));
            flowPane.getChildren().add(imageView);
            stage.setScene(new Scene(flowPane,150,150));
            stage.show();
        }
    }
