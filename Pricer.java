import java.io.*;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Scanner;
import java.util.TreeSet;


public class Pricer {
	private static int targetsize; //fill in from commandline
	private static TreeSet<Pricer.Entry> OrderBook;
	public Pricer(){};
//	private static PrintStream writer;
	
	public class Entry implements Comparable<Entry>{
		String oid, sd;
		double p;
		int sz, time;
		public Entry(int time, String Order_id, String side, double price, int size){
		this.oid = Order_id;
		this.sd = side;
		this.p = price;
		this.sz = size;
		this.time = time;
		}
		public int compareTo(Entry other) {
			if(this.oid == other.oid)
				return 0;
			if (this.p > other.p)
				return 1;
			if (this.p < other.p)
				return -1;
			if(this.p == other.p){
				//return this.time - other.time;
				//System.out.println(this.time + " " + this.p + " " + this.oid + " " + this.sd);
				//if they are both offers to sell, you want to buy the largest amount at the lowest price, so the larger order should be earlier in the book.
				//if they are both offers to buy, you want to sell the largest amount at the highest price, so the larger order should be later in the book. 
					if(this.sd.equals(other.sd)){
						if(this.sd.equals("S"))
							return ((this.sz >= other.sz) ? 1 : -1);
						if(this.sd.equals("B"))
							return ((this.sz >= other.sz) ? -1 : 1);
					}
					else{
						if(other.sd.equals("S"))
							return -1;
						else
							return 1;
					}
			
			}

			return 1;
		}
	}
	
	
	public static void main(String args[]){
//		PrintStream writer = new PrintStream(System.out);
		Scanner tsize = new Scanner(System.in);
		targetsize = tsize.nextInt();
		tsize.close();
		Scanner keyboard = null;
		try {
			keyboard = new Scanner(new BufferedReader(new FileReader("/Users/adamalloy/Downloads/pricer.in")));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // need to remove so not hard coded
		//Scanner keyboard = new Scanner(new BufferedReader(new FileReader("/Users/adamalloy/pricer2.in"))); // need to remove so not hard coded
		//Scanner keyboard = new Scanner("28800538 A b S 44.26 100\n28800562 A c B 44.10 100\n28800744 R b 100\n28800758 A d B 44.18 157\n28800773 A e S 44.38 100\n28800796 R d 157\n28800812 A f B 44.18 157\n28800974 A g S 44.27 100\n28800975 R e 100\n28812071 R f 100\n28813129 A h B 43.68 50\n28813300 R f 57\n28813830 A i S 44.18 100\n28814087 A j S 44.18 1000\n28814834 R c 100\n28814864 A k B 44.09 100\n28815774 R k 100\n28815804 A l B 44.07 175\n28815937 R j 1000\n28816245 A m S 44.22 100\n28816245 R g 100\n28816273 A n B 44.03 100\n28817570 A o S 44.14 170\n28822172 R o 20\n28823984 A p B 44.04 100\n28823984 R n 100\n28824454 R p 100\n28824484 A q B 44.03 100\n28826314 R q 100\n28826343 A r B 43.89 100\n28826384 R r 100\n28826414 A s B 43.78 100\n28826424 R s 100\n28826454 A t B 43.75 100\n28826455 R t 100\n28826485 A u B 43.72 100\n28835564 A v B 43.85 100\n28835565 R u 100\n28838797 A w S 44.15 500\n28841307 A x S 44.40 100\n28845097 A y S 44.10 500");
		OrderBook = new TreeSet<Entry>();

		Pricer pricer = new Pricer();
		Entry current = null;
		int[] BidsAsks = {0,0};
		boolean[] NA = {false, false};
		double[] last = {0.0, 0.0};
		while(keyboard.hasNextLine()){
			try {
				processLine(keyboard.nextLine(), BidsAsks, NA, last, pricer, current);
				//testSet();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		keyboard.close();
	}
		
		public static void processLine(String currentLine, int[] BidsAsks, boolean[] NA, double[] last, Pricer pricer, Entry current) throws IOException{
			//0: Bids 1: Asks
			Scanner parse = new Scanner(currentLine);
			int timestamp = Integer.parseInt(parse.next());
			String instruction = parse.next();
			if(instruction.equals("A")){
				current = pricer.new Entry(timestamp, parse.next(), parse.next(), Double.parseDouble(parse.next()), Integer.parseInt(parse.next()));
				AddOrder(current, BidsAsks, last, NA);
			}
			if(instruction.equals("R")){
				current = ReduceOrder(timestamp, parse.next(), Integer.parseInt(parse.next()), BidsAsks, NA);
				if(current.sd.equals("B")){
					last[0] = PrintAdd(current, BidsAsks[0], last[0], OrderBook.descendingIterator());
//					if(BidsAsks[0] < targetsize)
//						NA[0] = false;
				}
				if(current.sd.equals("S")){
					last[1] = PrintAdd(current, BidsAsks[1], last[1], OrderBook.iterator());
//					if(BidsAsks[1] < targetsize)
//						NA[1] = false;
				}
			}
}	
		
private static void AddOrder(Entry current, int[] BidsAsks, double[] last, boolean[] NA) throws IOException {
		OrderBook.add(current);
		if(current.sd.equals("B")){
			BidsAsks[0] += current.sz;
			last[0] = PrintAdd(current, BidsAsks[0], last[0], OrderBook.descendingIterator());
			if(BidsAsks[0] >= targetsize)
				NA[0] = true;
			}
		if(current.sd.equals("S")){
			BidsAsks[1] += current.sz;
			last[1] = PrintAdd(current, BidsAsks[1], last[1], OrderBook.iterator());
			if(BidsAsks[1] >= targetsize)
				NA[1] = true;
	}
}

//		writer.flush();
//		writer.close();
	


	private static double PrintAdd(Entry current, int num, double last, Iterator<Entry> iter) {
		double total = 0.0;
		if (num >= targetsize){
			int count = targetsize;
			Entry tmp; 
			while(count > 0 && iter.hasNext()){
				tmp = iter.next();
				//System.out.println("time: " + tmp.time + " order_id: " + tmp.oid + " side: " + tmp.sd + " price: " + tmp.p + " size: " + tmp.sz);
				if(tmp.sd.equals(current.sd)){
					if((count - tmp.sz) > 0){
						count -= tmp.sz;
						total += (tmp.p*tmp.sz);
					}
					else{
						total += (tmp.p*count);
						count = 0;
					}
					
				}
			}
			if(total != last){
			DecimalFormat decim = new DecimalFormat("0.00");
			String t = decim.format(total);
			System.out.println(current.time + " " + (current.sd.equals("S") ? "B" : "S")  + " " + t);
			}
		}
			return total;
}

	private static Pricer.Entry ReduceOrder(int timestamp, String O_id, int size, int[] BidsAsks, boolean[] NA) {
		// TODO Auto-generated method stub
		Iterator<Entry> iter =  OrderBook.iterator();
		Entry tmp = null;
		String tmp_id = "";
		while(iter.hasNext() && !tmp_id.equals(O_id)){
			tmp = iter.next();
			tmp_id = tmp.oid;
		}
		if(tmp.sd.equals("B"))
			BidsAsks[0]-= ((tmp.sz-size) <= 0) ? tmp.sz : size;
		else
			BidsAsks[1] -= ((tmp.sz-size) <= 0) ? tmp.sz : size;
		if((tmp.sz-size) <= 0){
			OrderBook.remove(tmp);	
		}
		else{
			tmp.sz -= size;
		}
		if((BidsAsks[0] < targetsize) && NA[0]){
			System.out.println(timestamp + " " + (tmp.sd.equals("S") ? "B" : "S") + " NA");
			NA[0] = false;
		}
		else if((BidsAsks[1] < targetsize) && NA[1]){
			System.out.println(timestamp + " " + (tmp.sd.equals("S") ? "B" : "S") + " NA");
			NA[1] = false;
		}
			
		tmp.time = timestamp;
		return tmp;
	}

	

/*	private static double PrintOrder(Entry current, int num, double last) throws IOException  {
		// TODO Auto-generated method stub
		//testSet();
		Iterator<Entry> iter;
		if (num >= targetsize){
			
			int count = targetsize;
			double total = 0.00;
			if(current.sd.equals("S")){
				iter =  OrderBook.iterator();
			}
			else{
				iter = OrderBook.descendingIterator();
			}
			Entry tmp; 
			while(count > 0 && iter.hasNext()){
				tmp = iter.next();

				if(tmp.sd.equals(current.sd)){
					if((count - tmp.sz) > 0){
						count -= tmp.sz;
						total += (tmp.p*tmp.sz);
					}
					else{
						total += (tmp.p*count);
						count = 0;
					}
					
				}
			}
			if(total != last){
			DecimalFormat decim = new DecimalFormat("0.00");
			String t = decim.format(total);
			System.out.println(current.time + " " + (current.sd.equals("S") ? "B" : "S")  + " " + t);
			}
			return total;
		}
		else
			return 0.0;
	}*/
	
	private static void testSet(){
		Entry tmp = null;
		Iterator<Entry> iter = OrderBook.iterator();
		while(iter.hasNext()){
			tmp = iter.next();
			System.out.println("timestamp: " + tmp.time + " order id: " + tmp.oid + " price: " + tmp.p + " size: " + tmp.sz + " side: " +tmp.sd);
		}
	}
	
	


}
