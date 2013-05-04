package rlmusic;

import gnu.trove.list.array.TDoubleArrayList;
import java.io.*;
import java.util.Random;

public class NeuralNetwork {
    
    private Random r;
    private double output;
    private float learningRate = 0.1f;
    private int numberOfHiddens;
    private TDoubleArrayList inputs;
    private TDoubleArrayList hiddens;
    private TDoubleArrayList inputWeights; //real valued
    private TDoubleArrayList hiddenWeights; //real valued
    private File mainfile;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private boolean closing = false;
    
    public NeuralNetwork() {
        r = new Random();
        inputs = new TDoubleArrayList();
        inputWeights = new TDoubleArrayList();
        hiddens = new TDoubleArrayList();
        hiddenWeights = new TDoubleArrayList();
        try {readFromFile(); initializeHiddens(); feedForward();} catch (IOException e) {System.out.println(e);}
        
    }
    
    public void initializeInputWeights() {
        for (int i = 0; i < inputs.size(); i++) {
            for (int j = 0; j < numberOfHiddens; j++) {
                inputWeights.add(r.nextGaussian());
            }
        }
        initializeHiddens();
        initializeHiddenWeights();
    }
    
    public void initializeHiddenWeights() {
        for (int j = 0; j < numberOfHiddens; j++) {
                hiddenWeights.add(r.nextGaussian());
        }
    }
    
    public void initializeHiddens() {
        for (int j = 0; j < numberOfHiddens; j++) {
                hiddens.add(r.nextFloat());
        }
    }
    
    public void setInputs(TDoubleArrayList values) {
        inputs.remove(0, inputs.size());
        inputs.addAll(values);
        numberOfHiddens = inputs.size()-1;
        if (inputWeights.isEmpty()) {initializeInputWeights();}
        else if (hiddens.isEmpty()) {initializeHiddens();}
    }
    
    public void train(double reward) {
        backPropagate((reward-output)*output*(1-output));
        feedForward();
    }
    
    public void backPropagate(double value) {
        for (int i = 0; i < hiddenWeights.size(); i++) {
            double newValue = learningRate*value*hiddens.get(i);
            hiddenWeights.set(i,hiddenWeights.get(i) + (newValue));
        }
        int size = inputs.size();
        for (int i = 0; i < numberOfHiddens; i++) {
            for (int j = 0; j < size; j++) {
                double newValue = learningRate*value*inputs.get(j);
                inputWeights.set(i*size + j,inputWeights.get(i*size + j) + (newValue));
            }
        }
    }
    
    public void feedForward() {
        int size = inputs.size();
            for (int i = 0; i < numberOfHiddens; i++) {
                double inputSum = 0;
                for (int j = 0; j < size; j++) {
                    inputSum += inputs.get(j)*inputWeights.get(i*size + j);
                }
                double unitOutput = 1.0/(1 + Math.pow(Math.E,-inputSum));
                if (Double.isNaN(unitOutput)) System.err.println("ANN NaN error");
                hiddens.set(i,unitOutput);
            }
            double hiddenSum = 0;
            for (int i = 0; i < numberOfHiddens; i++) {
                hiddenSum += hiddens.get(i)*hiddenWeights.get(i);
            }
            output = 1.0/(1 + Math.pow(Math.E,-hiddenSum));
    }
    
    public double predict() {
        return output;
    }
    
     public void writeToFile() throws IOException {
         if (!closing) {
            out = null;
            try {
                mainfile = new File("neuralNetwork.ann");
                mainfile.delete();
                mainfile.createNewFile();
                out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(mainfile)));
                out.writeObject(inputWeights);
                out.writeObject(hiddenWeights);
                out.close();
            } catch (IOException e) {
                System.out.println(e.toString());
                if (out != null) {
                    out.close();
                }
            } finally {
                if (out != null) {
                    out.close();
                }
            }
        }
    }
     
    public void readFromFile() throws IOException {
        if (!closing) {
            in = null;
            try {
                mainfile = new File("neuralNetwork.ann");
                if (mainfile.exists()) {
                in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(mainfile)));
                inputWeights = (TDoubleArrayList) in.readObject();
                hiddenWeights = (TDoubleArrayList) in.readObject();}
            } catch (ClassNotFoundException e) {
            } catch (IOException e) {
                if (in != null) {
                    in.close();
                }
            } finally {
                if (in != null) {
                    in.close();
                }
            }
        }
    }
    
    public void stopWriting() {
        if (hiddens.size() > 0) try {writeToFile();} catch (IOException e) {System.out.println("Write failed " + e);}
        closing = true;
        if (in != null) {try {in.close();} catch (IOException e) {System.out.println("Close failed.");}}
        if (out != null) {try {out.close();} catch (IOException e) {System.out.println("Close failed.");}}
    }
    
}
