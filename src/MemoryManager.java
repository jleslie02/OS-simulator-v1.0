import java.util.ArrayList;
public class MemoryManager {
	
	public static Disk disk;
	public static Memory ram;
	private static int totalPageNumber, totalFrameNumber;
	
	private final int PAGE_SIZE = 16;
	
	private ArrayList<Integer> freePageList, freeFrameList;
	
	
	public MemoryManager(){
		
		freeFrameList = new ArrayList<Integer>();
       
        disk = new Disk();
        ram = new Memory();
        
        totalPageNumber = 2048/PAGE_SIZE;
        totalFrameNumber = 1024/PAGE_SIZE;
        
        
        freePageList = new ArrayList<Integer>();
        
        
        initializeFreePageList();
        initializeFreeFrameList();
        
	}
	
	/**
	 * This method initialize freeFrameList
	 */
	private void initializeFreeFrameList() {
		for(int i =0; i<totalFrameNumber;i++){
			freeFrameList.add(i);
		}
		
	}

	/*
	 * This method initialize page table for the first time
	 */
	
	/**
	 * This method initialize freePageList
	 */
	private void initializeFreePageList(){
		for(int i=0;i<totalPageNumber;i++){
			freePageList.add(i);
		}
	}
	
	public int getFreeFrame(PCB pcb){
		return 0;
		
	}
	
	public void firstData(PCB pcb){
		int frame = getFreeFrameNumber();
		int page = getFreePageNumber();
		int diskAddress = pcb.diskFileAddress;
		int memoryAddress = frame * getPAGE_SIZE();
		for(int i = 0 ;i<getPAGE_SIZE();i++){
			ram.writeData(memoryAddress+i, disk.readData(diskAddress+i));
		}
		pcb.pageTable[0].setFrameNumber(frame);
		pcb.pageTable[0].setValid(true);
		pcb.pageTable[0].setJobID(pcb.processId);
		
	}
	public int getPhysicalAddress(int offset, PCB pcb){
		int physicalAddress;
		int index = offset/getPAGE_SIZE();
		int remainder = offset%getPAGE_SIZE();
		if(pcb.pageTable[index].isValid()==true){
			physicalAddress = pcb.pageTable[index].getFrameNumber()*getPAGE_SIZE()+ remainder;
		} else {
			pcb.setPageFault(pcb.getPageFault()+1);
			pageFault(index,pcb);
			physicalAddress = pcb.pageTable[index].getFrameNumber()*getPAGE_SIZE()+ remainder;
		}
		return physicalAddress;
				
	}
	/**
	 * A method that writes data to the ram.
	 * @param takes offset address of logical memory
	 * @param takes pcb
	 * @param register to write in ram
	 */
	public void writeData(int offset,PCB pcb, long register){
		int physicalAddress = getPhysicalAddress(offset, pcb);
		ram.writeData(physicalAddress, register);
	}
	
	/**
	 * Fetches binary data from ram
	 * @param takes offset address of logical memory
	 * @param takes pcb
	 * @return boolean array of binary data from ram
	 */
	public boolean[] fetchData(int offset, PCB pcb){
		int physicalAddress = getPhysicalAddress(offset,pcb);
		return readRamDataBinary(physicalAddress);
	}
	
	/**
	 * Fetches data as a long from ram
	 * @param takes offset address of logical memory
	 * @param takes pcb
	 * @return long type data from ram
	 */
	public long fetchLongData(int offset, PCB pcb){
		int physicalAddress = getPhysicalAddress(offset,pcb);
		return readRamData(physicalAddress);
	}
	private void pageFault(int index, PCB pcb) {
		int frame = getFreeFrameNumber();
		int page = getFreePageNumber();
		pcb.pageTable[index].setFrameNumber(frame);
		pcb.pageTable[index].setValid(true);
		pcb.pageTable[index].setJobID(pcb.processId);
		int tempIndex = pcb.getMemoryFootprint()/getPAGE_SIZE();
		int remainder = pcb.getMemoryFootprint()%getPAGE_SIZE();
		int stop;
		int diskAddress = pcb.diskFileAddress+index*getPAGE_SIZE();
		if((remainder>0) && (index == tempIndex)){
			stop = diskAddress+remainder;
		} else {
			stop = diskAddress+getPAGE_SIZE();
		}
		
		int memoryAddress = frame * getPAGE_SIZE();
		for(int i =diskAddress;i<stop;i++){
			ram.writeData(memoryAddress, disk.readData(i));
			memoryAddress++;
		}
		
		
	}

	/**
	 * This methods gives the first free page number
	 * returns -1 if pages are not free
	 */
	public int getFreePageNumber(){
		if(freePageList.size()>0){
			return freePageList.remove(0);
		}
		return -1;
	}
	
	public int getPAGE_SIZE() {
		return PAGE_SIZE;
	}

	/**
	 * This methods gives the first free frame number
	 * returns -1 if frames are not free
	 */
	public int getFreeFrameNumber(){
		if(freeFrameList.size()>0){
			return freeFrameList.remove(0);
		}
		return -1;
	}
	
	
	public void freeMemory(PCB pcb){
		int length = pcb.pageTable.length;
		for(int i =0;i<length;i++){
			if(pcb.pageTable[i].isValid()==true){
				int frame = pcb.pageTable[i].getFrameNumber();
				pcb.pageTable[i].setValid(false);
				pcb.pageTable[i].setJobID(0);
				pcb.pageTable[i].setFrameNumber(0);
				freeFrame(frame);
				addFrameList(frame);				
			}
		}
		
	}
	public void addFrameList(int frameNumber){
		freeFrameList.add(frameNumber);
		
	}
	/**
	 * This methods free frame from page table, and also free memory of ram
	 * @param frameNumber that would be free
	 */
	public void freeFrame(int frameNumber){
		int address = frameNumber*PAGE_SIZE;
		int length = PAGE_SIZE;
		
		
		
		ram.free(address, length);
	}
	/****************************************************************************************
	* Reading and writing data to the disk and ram methods									*
	* 																						*
	*****************************************************************************************/
	public void writeDiskData(int address, long data){
		disk.writeData(address, data);
	}
	
	public void writeRamData(int address, long data){
		ram.writeData(address, data);
	}
	
	public void writeDiskDataBinary(int address, boolean[] data){
		disk.writeBinaryData(address, data);
	}
	
	public void writeRamDataBinary(int address, boolean[] data){
		ram.writeBinaryData(address, data);
	}
	
	public boolean[] readRamDataBinary(int address){
		return ram.readBinaryData(address);
	}
	
	public boolean[] readDiskDataBinary(int address){
		return disk.readBinaryData(address);
	}
	
	public long readRamData(int address){
		return ram.readData(address);
	}
	
	public long readDiskData(int address){
		return disk.readData(address);
	}
	/****************************************************************
	 * Finished with reading and writing to disk and ram methods	*
	 ****************************************************************/
}
