package org.davidliebman.android;

import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by dave on 1/19/16.
 */
public class Operation {
    Network network;
    MultiLayerNetwork model;
    DataSetSplit data;
    DataSetIterator train, test;
    int batchSize;
    int epochs;
    int iterations;

    int evalType = 0;

    public static final int EVAL_SINGLE_NUMERIC = 1;
    public static final int EVAL_SINGLE_ALPHA = 2;
    public static final int EVAL_TRAIN_NUMERIC = 3;
    public static final int EVAL_TRAIN_ALPHA = 4;

    private static final Logger log = LoggerFactory.getLogger(Operation.class);


    public Operation(Network model, DataSetSplit data, int batchSize, int epochs, int iterations) throws Exception{
        this.network = model;
        this.model = model.getModel();
        this.data = data;
        this.batchSize = batchSize;
        this.epochs = epochs;
        this.iterations = iterations;

    }

    public Operation(Network model) throws Exception {
        this.network = model;
        this.model = model.getModel();


    }

    public void setEvalType(int type) {evalType = type;}

    public void startOperation() throws Exception {

        switch (evalType) {
            case EVAL_SINGLE_ALPHA:
                break;
            case EVAL_SINGLE_NUMERIC:
                break;
            case EVAL_TRAIN_ALPHA:
                break;
            case EVAL_TRAIN_NUMERIC:
                startOperationMnistTrain();
                break;
        }

    }

    public void startOperationMnistTrain() throws Exception {

        boolean saveValues = false;
        boolean loadValues = true;
        boolean trainValues = false;
        boolean evalValues = true;

        log.info("Load data....");
        //DataSetSplit mnist = new DataSetSplit();

        train = data.getSetTrain();
        test = data.getSetTest();

        FileManager files = new FileManager();

        if (loadValues) {
            files.loadModel(model);
        }

        log.info("Train model....");
        model.setListeners(new ScoreIterationListener(1));
        for( int i=0; i<epochs; i++ ) {

            if (trainValues) model.fit(train);

            log.info("*** Completed epoch {} ***", i);
            if (evalValues) {
                log.info("Evaluate model....");
                Evaluation eval = new Evaluation(network.getOutputNum());
                while (test.hasNext()) {
                    DataSet ds = test.next();
                    INDArray output = model.output(ds.getFeatureMatrix());
                    eval.eval(ds.getLabels(), output);
                }
                log.info(eval.stats());
                test.reset();
            }
        }
        log.info("****************Example finished********************");
        // 38 mins, 0.9446 Accuracy

        if(saveValues && trainValues) {
            files.saveModel(model);
            log.info("values saved....");
        }
    }
}