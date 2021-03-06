package org.davidliebman.android;


        import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
        import org.deeplearning4j.eval.Evaluation;
        import org.deeplearning4j.nn.api.Layer;
        import org.deeplearning4j.nn.api.OptimizationAlgorithm;
        import org.deeplearning4j.nn.conf.GradientNormalization;
        import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
        import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
        import org.deeplearning4j.nn.conf.Updater;
        import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
        import org.deeplearning4j.nn.conf.layers.DenseLayer;
        import org.deeplearning4j.nn.conf.layers.OutputLayer;
        import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
        import org.deeplearning4j.nn.conf.layers.setup.ConvolutionLayerSetup;
        import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
        import org.deeplearning4j.nn.weights.WeightInit;
        import org.deeplearning4j.optimize.api.IterationListener;
        import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
        import org.nd4j.linalg.api.buffer.DataBuffer;
        import org.nd4j.linalg.api.ndarray.INDArray;
        import org.nd4j.linalg.dataset.DataSet;
        import org.nd4j.linalg.dataset.SplitTestAndTrain;
        import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
        import org.nd4j.linalg.dataset.api.iterator.MultipleEpochsIterator;
        import org.nd4j.linalg.factory.Nd4j;
        import org.nd4j.linalg.lossfunctions.LossFunctions;
        import org.slf4j.Logger;
        import org.slf4j.LoggerFactory;

        import java.io.*;
        import java.nio.file.Files;
        import java.nio.file.Paths;
        import java.util.ArrayList;
        import java.util.List;
        import java.util.Random;

/**
 * Created by agibsonccc on 9/16/15.
 */
public class LenetMnistExample {
    private static final Logger log = LoggerFactory.getLogger(LenetMnistExample.class);

    public static void main(String[] args) throws Exception {
        int nChannels = 1;
        int outputNum = 10;
        int batchSize = 64;
        int nEpochs = 1;//10
        int iterations = 1;
        int seed = 123;
        int testnum = 12345; // 12345
        boolean saveValues = true;
        boolean loadValues = true;
        boolean trainValues = true;
        boolean evalValues = true;
        String fileName = "/home/dave/workspace/lenet_example_digits.bin";

        log.info("Load data....");
        DataSetIterator mnistTrain = new MnistDataSetIterator(batchSize,true,testnum);
        DataSetIterator mnistTest = new MnistDataSetIterator(batchSize,false,testnum);

        log.info("Build model....");
        MultiLayerConfiguration.Builder builder = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .iterations(iterations)
                .regularization(true).l2(0.0005)
                .learningRate(0.01)
                .weightInit(WeightInit.XAVIER)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(Updater.NESTEROVS).momentum(0.9)
                .list(4)
                .layer(0, new ConvolutionLayer.Builder(5, 5)
                        .nIn(nChannels)
                        .stride(1, 1)
                        .nOut(20).dropOut(0.5)
                        .activation("relu")
                        .build())
                .layer(1, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2,2)
                        .stride(2,2)
                        .build())
                .layer(2, new DenseLayer.Builder().activation("relu")
                        .nOut(500).build())
                .layer(3, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nOut(outputNum)
                        .activation("softmax")
                        .build())
                .backprop(true).pretrain(false);
        new ConvolutionLayerSetup(builder,28,28,1);

        MultiLayerConfiguration conf = builder.build();
        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();

        if (loadValues) {
            File filePath = new File(fileName);
            DataInputStream dis = new DataInputStream(new FileInputStream(filePath));
            INDArray newParams = Nd4j.read(dis);
            dis.close();
            model.setParameters(newParams);
        }

        log.info("Train model....");
        model.setListeners(new ScoreIterationListener(1));
        for( int i=0; i<nEpochs; i++ ) {

            if (trainValues) model.fit(mnistTrain);

            log.info("*** Completed epoch {} ***", i);
            if (evalValues) {
                log.info("Evaluate model....");
                Evaluation eval = new Evaluation(outputNum);
                while (mnistTest.hasNext()) {
                    DataSet ds = mnistTest.next();
                    INDArray output = model.output(ds.getFeatureMatrix());
                    eval.eval(ds.getLabels(), output);
                }
                log.info(eval.stats());
                mnistTest.reset();
            }
        }
        log.info("****************Example finished********************");
        // 38 mins, 0.9446 Accuracy

        if(saveValues && trainValues) {
            //Write the network parameters:
            File filePointer = new File(fileName);
            //OutputStream fos = Files.newOutputStream(Paths.get(fileName));
            FileOutputStream fos = new FileOutputStream(filePointer);
            DataOutputStream dos = new DataOutputStream(fos);
            Nd4j.write(model.params(), dos);
            dos.flush();
            dos.close();
            log.info("values saved....");
        }
    }
}

