package snapshot;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import org.opencv.core.*;

import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static snapshot.VideoHandler.drawImageOverSecondImage;
import static snapshot.VideoHandler.onFXThread;
import static snapshot.VideoHandler.toFxImage;

public class Controller {


    @FXML
    private ImageView frame;

    private VideoCapture videoCapture = new VideoCapture();
    private ScheduledExecutorService timer;
    private Mat mat = new Mat();
    private Mat mat2 = new Mat();
    private volatile boolean isRunning;
    private int colorId;


    @FXML
    protected void takeSnapShot() {
        if (videoCapture.isOpened()) {
            turnOffCamera();
            Date date = new Date();
            String formattedDate = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(date);
            setRunning(true);
            initializeCamera(this);
            Imgcodecs.imwrite("snapshot-" + formattedDate + ".png", getMat());
        } else {
            System.err.print("Camera is Off\n");
        }

    }

    @FXML
    protected void turnNormalFilter() {
        turnOffCamera();
        setColorId(Imgproc.COLOR_RGBA2RGB);
        setRunning(true);
        initializeCamera(this);
    }

    @FXML
    protected void turnBlackWhiteFilter() {
        turnOffCamera();
        setColorId(Imgproc.COLOR_BGR2GRAY);
        setRunning(true);
        initializeCamera(this);

    }

    @FXML
    protected void turnSuperFilter() {
        turnOffCamera();
        setRunning(true);
        setColorId(Imgproc.COLOR_RGB2HSV);
        initializeCamera(this);
    }


    void initializeCamera(Controller controller) {
        videoCapture.open(0);
        if (isRunning) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    Mat image = getMat();
                    toFxImage(image);
                    faceDetection();
                    onFXThread(controller.getFrame().imageProperty(), toFxImage(image));

                }
            };
            timer = Executors.newSingleThreadScheduledExecutor();
            timer.scheduleAtFixedRate(runnable, 0, 33, TimeUnit.MILLISECONDS);
        } else {
            timer.shutdown();
            videoCapture.release();
        }
    }


    private Mat getMat() {
        videoCapture.read(mat);
        if (this.mat.channels() > 1) {
            Imgproc.cvtColor(mat, mat2, getColorId());
            return mat2;
        } else {
            Imgproc.cvtColor(mat, mat, getColorId());
        }
        return mat;
    }

    private void turnOffCamera() {
        setRunning(false);
        if (videoCapture.isOpened())
            initializeCamera(this);

    }

    private void faceDetection() {
        Mat grayMat = new Mat();
        MatOfRect matOfRect = new MatOfRect();
        CascadeClassifier faceClassifier = new CascadeClassifier();
        faceClassifier.load("C:\\Users\\Lelental\\OneDrive\\Dokumenty" +
                "\\SnapShot\\src\\snapshot\\haarcascade_frontalface_alt.xml");


        if (getMat().channels() > 1) {
            Imgproc.cvtColor(getMat(), grayMat, Imgproc.COLOR_BGR2GRAY);
            showFace(grayMat, faceClassifier, matOfRect);
            printRectangleOnFace(matOfRect, getMat());

        } else {
            showFace(getMat(), faceClassifier, matOfRect);
            printRectangleOnFace(matOfRect, getMat());
        }

    }

    private void showFace(Mat obtainedMat, CascadeClassifier obtainedClassifier, MatOfRect matOfRect) {
        Imgproc.equalizeHist(obtainedMat, obtainedMat);
        obtainedClassifier.detectMultiScale(obtainedMat, matOfRect, 1.1, 2,
                Objdetect.CASCADE_SCALE_IMAGE, new Size(30, 30), new Size());

    }

    private void printRectangleOnFace(MatOfRect faces, Mat obtainedMat) {
        Rect[] facesArray = faces.toArray();
        Mat glasses = Imgcodecs.imread("C:\\Users\\Lelental\\OneDrive\\Dokumenty\\SnapShot\\src\\snapshot\\dwi.png");

        for (Rect aFacesArray : facesArray) {
            Imgproc.rectangle(obtainedMat, aFacesArray.tl(), aFacesArray.br(),
                    new Scalar(0, 255, 0, 255), 3);

            CascadeClassifier cascadeClassifier = new CascadeClassifier();
            MatOfRect eyesMatOfRect = new MatOfRect();
            Rect[] eyes2 = eyesMatOfRect.toArray();
            cascadeClassifier.load("C:\\Users\\Lelental\\OneDrive\\Dokumenty\\SnapShot\\src\\snapshot\\haarcascade_eye_tree_eyeglasses.xml");
            cascadeClassifier.detectMultiScale(obtainedMat, eyesMatOfRect, 1.1, 2, Objdetect.CASCADE_SCALE_IMAGE,
                    new Size(30, 30), new Size());

            if (eyes2.length > 0) {
                MatOfPoint2f dst = new MatOfPoint2f();
                MatOfPoint2f helpVariable = new MatOfPoint2f();
                helpVariable.put(aFacesArray.x, aFacesArray.y + (aFacesArray.height * 5 / 20));
                dst.push_back(helpVariable);
                helpVariable.put(aFacesArray.x + aFacesArray.width, aFacesArray.y + aFacesArray.height * 5 / 20);
                dst.push_back(helpVariable);
                helpVariable.put(aFacesArray.x + aFacesArray.width, aFacesArray.y + aFacesArray.height * 5 / 20 + aFacesArray.height * 3 / 10);
                dst.push_back(helpVariable);
                helpVariable.put(aFacesArray.x, aFacesArray.y + aFacesArray.height * 5 / 20 + aFacesArray.height * 3 / 10);
                dst.push_back(helpVariable);

                drawImageOverSecondImage(glasses.clone(), obtainedMat, dst);
            }
        }
    }


    public ImageView getFrame() {
        return frame;
    }

    public int getColorId() {
        return colorId;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public void setColorId(int colorId) {
        this.colorId = colorId;
    }

}
