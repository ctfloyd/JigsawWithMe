
public class NotifierThread  extends Thread {
	
	Runnable proc;
	ThreadResolver callback;
	
	public NotifierThread(Runnable r, ThreadResolver callback) {
		super(r);
		proc = r;
		this.callback = callback;
	}
	
	@Override
	public final void run() {
		try {
			proc.run();
		} finally {
			synchronized(callback) {
				callback.resolve(this);
			}
		}
	}
}
