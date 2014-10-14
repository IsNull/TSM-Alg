package mse.alg.ex3.PicsiSWT.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

public class Parallel {
	public static interface IntLoopBody {
	    void run(int i);
	}
	
	public static interface LoopBody<T> {
	    void run(T i);
	}

	public static interface RedDataCreator<T> {
		T run();
	}
	
	public static interface RedLoopBody<T> {
	    void run(int i, T data);
	}
	
	public static interface Reducer<T> {
	    void run(T returnData, T addData);
	}
	
	private static class ReductionData<T> {
		Future<?> future;
		T data;
	}
	
	static final int nCPU = Runtime.getRuntime().availableProcessors();

	public static <T> void ForEach(Iterable <T> parameters, final LoopBody<T> loopBody) {
	    ExecutorService executor = Executors.newFixedThreadPool(nCPU);
	    List<Future<?>> futures  = new LinkedList<>();

	    for (final T param : parameters) {
	    	futures.add(executor.submit(() -> loopBody.run(param) ));
	    }

        for (Future<?> f : futures) {
        	try { 
        		f.get();
		    } catch (InterruptedException | ExecutionException e) { 
		    	System.out.println(e); 
		    }
        }
	    executor.shutdown();     
	}

	public static void For(int start, int stop, final IntLoopBody loopBody) {
	    final int chunkSize = (stop - start + nCPU - 1)/nCPU;
        final int loops = (stop - start + chunkSize - 1)/chunkSize;
        ExecutorService executor = Executors.newFixedThreadPool(loops);
        List<Future<?>> futures  = new LinkedList<>();

        for (int i=start; i < stop; ) {
            final int iStart = i;
            i += chunkSize;
            final int iStop = (i < stop) ? i : stop;
            
	        futures.add(executor.submit(() -> {
            	for (int j = iStart; j < iStop; j++) 
            		loopBody.run(j);
            }));     
	    }

        for (Future<?> f : futures) {
        	try { 
        		f.get();
		    } catch (InterruptedException | ExecutionException e) { 
		    	System.out.println(e); 
		    }
        }
	    executor.shutdown();     
	}

	public static <T> void For(int start, int stop, T result, final RedDataCreator<T> creator, final RedLoopBody<T> loopBody, final Reducer<T> reducer) {
	    final int chunkSize = (stop - start + nCPU - 1)/nCPU;
        final int loops = (stop - start + chunkSize - 1)/chunkSize;
        ExecutorService executor = Executors.newFixedThreadPool(loops);
        List<ReductionData<T>> redData  = new LinkedList<>();

        for (int i = start; i < stop; ) {
            final int iStart = i;
            i += chunkSize;
            final int iStop = (i < stop) ? i : stop;
            final ReductionData<T> rd = new ReductionData<>();
                        
            rd.data = creator.run();
	        rd.future = executor.submit(() -> {
	            for (int j = iStart; j < iStop; j++) {
	            	loopBody.run(j, rd.data);
	            }
	        });
	        redData.add(rd);
	    }

        for (ReductionData<T> rd : redData) {
        	try { 
        		rd.future.get();
        		if (rd.data != null) {
        			reducer.run(result, rd.data);
        		}
		    } catch (InterruptedException | ExecutionException e) { 
				e.printStackTrace();
		    }
        }
	    executor.shutdown();     
	}
}


