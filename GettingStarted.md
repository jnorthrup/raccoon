## Introduction ##

This page gives a brief introduction to Raccoon to help you get started developing your own code.


## Engine Instantiation ##

The main class that implements the coding engine is `org.msrg.raccoon.engine.CodingEngineImpl` which implements the `ICodingEngine` interface. To start, you need to instantiate a Raccoon engine by specifying the number of threads in its thread pool:
```
int threads = 4;
ICodingEngine engine = new CodingEngineImpl(threads);
```

Next, you need to call `init()` in order to initialize and allocate the engine threads:
```
engine.init();
```

Finally, to get the threads pool initialization complete, you need to call:
```
engine.startComponent();
```


## Coding Tasks ##

The engine operates in an asynchronous manner, that is, after you invoke an operation you immediately receive a handle which you can later use to retrieve the results. In the meantime, the engine schedules to execute the operation asynchronously. Once the operation is complete, you receive a callback that indicates the corresponding status change. To receive updates on status change of your operations, e.g., completion, error, start to execute, you need to register a listener. The listener must implement `org.msrg.raccoon.engine.ICodingListener` and provide the logic to react to operation status change. For example, once the operation is completed successfully, it must retrieve the results. The listener is registered via the engine's `registerCodingListener(ICodingListener listeners)` method:
```
ICoding listener; // Your implementation
engine.registerCodingListener(listener);
```


## Coding Operations ##

The operations that are supported by the engine are defined in `org.msrg.raccoon.engine.ICodingEngine` and include data encoding and decoding, matrix multiplication, inversion (in Galois Field of G^8) and equality check. These operations are as follows:
```
  public CodedSlice_CodingResult encode(ICodingListener listener, CodedBatch codeBatch);
  public Equals_CodingResult decode(ICodingListener listener, ReceivedCodedBatch codeBatch);
	
  public BulkMatrix_CodingResult multiply(ICodingListener listener, ByteMatrix m, BulkMatrix bm);
  public SliceMatrix_CodingResult multiply(ICodingListener listener, ByteMatrix1D m, BulkMatrix bm);
	
  public ByteMatrix_CodingResult inverse(ICodingListener listener, ByteMatrix m);
  public Equals_CodingResult checkEquality(ICodingListener listener, SliceMatrix sm1, SliceMatrix sm2);
  public Equals_CodingResult checkEquality(ICodingListener listener, BulkMatrix bm1, BulkMatrix bm2);
```

## Your First Piece of Code ##

The following is a simple code that executes a simple multiplication operation:

```

import org.msrg.raccoon.engine.ICodingEngine;
import org.msrg.raccoon.engine.ICodingListener;
import org.msrg.raccoon.engine.CodingEngineImpl;

import org.msrg.raccoon.matrix.bulk.BulkMatrix;
import org.msrg.raccoon.matrix.finitefields.ByteMatrix;

import org.msrg.raccoon.engine.task.result.BulkMatrix_CodingResult;
import org.msrg.raccoon.engine.task.result.CodingResult;


class MyCodingListener implements ICodingListener {

  protected ICodingEngine engine;

  public MyCodingListener() {
    engine = new CodingEngineImpl(4);
    engine.init();
    engine.registerCodingListener(this);
    engine.startComponent();
  }

  protected void invokeMultiplication() {
    byte[][] b1 = new byte[10][100];
    for(int i=0 ; i<b1.length ; i++) {
    b1[i] = new byte[100];
    for(int j=0 ; j<b1[i].length ; j++)
      b1[i][j] = (byte) (i+j);
    }
    ByteMatrix m = new ByteMatrix(b1); // Initialize byte matrix data

    byte[][] b2 = new byte[100][10000];
    for(int i=0 ; i<b2.length ; i++) {
      b2[i] = new byte[100];
      for(int j=0 ; j<b2[i].length ; j++)
        b2[i][j] = (byte) (i+j);
    }
    BulkMatrix bm = new BulkMatrix(b2);

    BulkMatrix_CodingResult result = engine.multiply(this, m, bm);
    // Retain the results handle
  }

  public void codingFinished(CodingResult result) {
    switch (result._resultsType) {
      case BULK_MATRIX:
      {
        BulkMatrix_CodingResult smResult = (BulkMatrix_CodingResult) result;
        BulkMatrix sm = smResult.getResult();
        // Do something with the result of the multiplication.
        break;
      }

      case BOOLEAN:
      case CODED_SLICE_MATRIX:
      default:
        break;
    }
  }

  public void codingFailed(CodingResult result) {
    // Handle errors...
  }

  public void codingStarted(CodingResult result) {}
  public void codingPreliminaryStageCompleted(CodingResult result){}
}
```