import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.List;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

public class Analysis {

	public static void main(String[] args) throws IOException {
		
		// Setup the root and the main directory
		// Change this for your own files location
		// Setup the minimal lateral roots length, below this value we do not consider the lateral or
		// secondary root
		
		String root = "Z:";
		String workDir = "W_2011_09_21";
		//String workDir = "W_2011_All_In_One";
		Double minLateralRootLength = 0.1;
		
		// Setup the working directories inside the main directory
		// 1_Input : directory to place the files to be analyzed
		// 2_Cleanup : directory where the cleaned up files are stored
		// 3_Output : for each input file a CSV file will be created and placed in this directory
		// 4_Final : directory with all the final files
		
		String inputDir = root+"\\"+workDir+"\\1_Input\\";
		String cleanupDir = root+"\\"+workDir+"\\2_Cleanup\\";
		String outputDir = root+"\\"+workDir+"\\3_Output\\";
		String finalDir = root+"\\"+workDir+"\\4_Final\\";
		
		File dir = new File(inputDir);	
		String[] children = dir.list();
	    List<Accession> accessionsList = new ArrayList<Accession>();
		
		if (children == null) {
		} else {
		    for (int i=0; i<children.length; i++) { // Loop in the directory for the files to be treated

		    	// Get the accession name out of the file name
		    	int pointIndex = children[i].indexOf(".");
			    String accession = children[i].substring(0, pointIndex);
			    //System.out.println(accession);
			    
			    // Build the different file names
			    // String inputFileName = inputDir+accession+".bmp.txt";
			    String inputFileName = inputDir+accession+".txt";
				String cleanupFileName = cleanupDir+accession+".txt";
				String outputFileName = outputDir+accession+".csv";
				
				// Clean up the input file and store it
				File inFile = new File(inputFileName);
				cleanup(inFile,cleanupFileName);
				
				// Parse the file we have cleaned up to extract the required data
				// Build a .csv file for each accession containing the extracted data
				inFile = new File(cleanupFileName);
				Accession myAccession = new Accession();
				myAccession = parse(inFile,outputFileName,minLateralRootLength);			    
			    accessionsList.add(myAccession);
			    
		    }		    
			
		    // Write final file #1 
		    String outFileName = finalDir+"Accessions_01.csv";
		    
		    // Compute from all the data parsed the global means
		    // globalMeans[0] is for the Main Root Length
		    // globalMeans[1] is for the Number of Lateral Roots
		    // globalMeans[2] is for the Sum of Lateral Roots Length 
		    
		    Double[] globalMeans = new Double[3];
		    globalMeans = writeFile2(outFileName,accessionsList);
		    	    
			// Determine the unique Accessions names
			List<String> accessionsNameList = new ArrayList<String>();
			accessionsNameList = getUniqueAccessionsNames(accessionsList);
			
			// Write sorted output files
			writeFilesPerConcentration(finalDir,"10µM",accessionsNameList,accessionsList);
			writeFilesPerConcentration(finalDir,"10mM",accessionsNameList,accessionsList);
			
			// Write corrected output files
			writeCorrectedFilesPerConcentration(finalDir,"10µM",accessionsNameList,accessionsList,globalMeans);
			writeCorrectedFilesPerConcentration(finalDir,"10mM",accessionsNameList,accessionsList,globalMeans);
			    
		}				
	}

	public static void cleanup(File infile,String cleanupfilename){
		
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		DataInputStream dis = null;
		
		try {
			fis = new FileInputStream(infile);
		    bis = new BufferedInputStream(fis);
		    dis = new DataInputStream(bis);
		    FileWriter f0 = new FileWriter(cleanupfilename);
		    
		    while (dis.available() != 0) {	    	
		    	String line = dis.readLine();
		    	//System.out.println(line);
		    	String tmpLine1 = line.replace("\t", ";");
		    	//System.out.println(tmpLine1);
		    	String tmpLine2 = tmpLine1.replace(": ", ";");
		    	//System.out.println(tmpLine2);
		    	String tmpLine3 = tmpLine2+"\r\n";
		    	//System.out.println(tmpLine3);
		    	f0.write(tmpLine3);		    	
		    }
		    
			f0.close();			
		    fis.close();
		    bis.close();
		    dis.close();
		    
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Accession parse(File infile,String outputfilename, Double minlateralrootlength){
		
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		DataInputStream dis = null;
		
		// variables to store the accessions data
		String fileName;
		String experimentName;
		String boxName;
		int nbOfPlants;

	    Accession currentAccession = new Accession();
	    Accession parsedAccession = new Accession();
		
		try {
			fis = new FileInputStream(infile);
		    bis = new BufferedInputStream(fis);
		    dis = new DataInputStream(bis);
		    
		    while (dis.available() != 0) {
		    	
		    	// Get the accession file name, we only take the text before the .bmp
		    	String line = dis.readLine();
		    	//System.out.println(line);
			    
			    // skip line with user name
			    dis.readLine();
			    
			    // Get the accession experiment name
			    line = dis.readLine();
			    //System.out.println(line);
			    experimentName = getStringLineItem(line,1,";");
			    //System.out.println(experimentName);
			    currentAccession.setExperimentName(experimentName);
			    //System.out.println(experimentName);
			    
			    // Extract the accession name, the concentration, the box out of the experiment name
			    
			    String str1 = experimentName;
			    //System.out.println(str1);
			    String str11 = str1.replace(" ", ";");
			    //System.out.println(str11);
		    	String[] fields11 = str11.split(";");
		    	
		    	String currentAccessionName;
		    	String currentConcentration;
		    	String currentBox;
		    	
		    	//System.out.println(fields11.length);
		    	
		    	if (fields11.length == 3){
		    		currentAccessionName = fields11[0].toUpperCase();
		    		currentConcentration = fields11[1];
		    		currentBox = fields11[2].toUpperCase();
		    	} else {
		    		currentAccessionName = fields11[0].toUpperCase()+" "+fields11[1].toUpperCase();
		    		currentConcentration = fields11[2];
		    		currentBox = fields11[3].toUpperCase();
		    	}
		    	currentAccession.setAccessionName(currentAccessionName);
		    	currentAccession.setConcentration(currentConcentration);
		    	currentAccession.setBox(currentBox);
			    
			    // Get the accession box name
			    line = dis.readLine();
			    boxName = getStringLineItem(line,1,";");
			    currentAccession.setBoxName(boxName);
			    //System.out.println(boxName);
			     
			    // skip lines with Genotype,Media, Age of Plants
			    dis.readLine();
			    dis.readLine();
			    dis.readLine();
			    
			    // Get the accession number of plants
			    line = dis.readLine();
			    nbOfPlants = getIntegerLineItem(line,1,";");
			    currentAccession.setNbOfPlants(nbOfPlants);
			    //System.out.println(nbOfPlants);
			    
			    // skip line with scale and 3 blank lines
			    dis.readLine();
			    dis.readLine();
			    dis.readLine();
			    dis.readLine();

			    // We need to extract for each plant in the accession:
			    // - the main root length
			    // - the number of lateral roots
			    // - the sum of all the lateral and their secondary roots length
			    //
			    Double[] mainRootLength = new Double[nbOfPlants];
				int[] nbOfLateralRoots = new int[nbOfPlants];
				Double[] sumOfLatRootsLength = new Double[nbOfPlants];
			    
			    for (int i = 0; i < nbOfPlants; i++) {
			    	
			    	// Skip line with the root identification (Root i)
			    	dis.readLine();
			    	
			    	// Get the Main root length
			    	line = dis.readLine();
			    	mainRootLength[i] = getDoubleLineItem(line,1,";");
				    //System.out.println(roundDouble(rootLength[i]));
				    
			    	// Skip lines with Main root vector, Main root angle
				    dis.readLine();
				    dis.readLine();
				    
				    // Get the Number lateral root(s)
				    line = dis.readLine();
				    nbOfLateralRoots[i] = getIntegerLineItem(line,1,";");
				    
				    // We will now get the length of each lateral root and of its secondary roots
				    // we will sum those lengths only for roots having a length greater than a 
				    // specified limit
				    
				    Double lateralRootsLenghSum = 0.00;
				    Double tempValue = 0.00;
				    int maxNbOfRoots = nbOfLateralRoots[i];
				    
				    // The number of lines to read is a function of the number of the lateral roots
				    for (int j = 0; j < maxNbOfRoots; j++) {
				    	
				    	// read the line with the lateral root data
				    	// and extract all the fields of that line
				    	line = dis.readLine();
				    	String[] lineFields = getFields(line,";");
				    	int nbOfFields = lineFields.length;
				    	
				    	// check if we are looking at a lateral root
				    	// if yes and the length is below the minimal root length limit
				    	// then we need to decrease the number of lateral roots
				    	
				    	if (nbOfFields == 12){
				    		
					    	// Get the length of the lateral root
					    	for (int k = 0; k < nbOfFields; k++ ){
					    		if (lineFields[k].contains("Length")){
					    			Double rootLength = Double.valueOf(lineFields[k+1].replace(",", "."));
					    			//System.out.println(rootLength);
					    			if (rootLength <= minlateralrootlength){
					    				nbOfLateralRoots[i] = nbOfLateralRoots[i] -1;
					    			} 
					    		}
					    	}
				    	}
				    	
				    	// Get the Number secondary root(s)
				    	int nbOfSecondaryRoots = Integer.parseInt(lineFields[nbOfFields-1]);
				    	//System.out.println(nbOfSecondaryRoots);
				    	
				    	// if the value of the number of secondary roots is not 0 then 
				    	// we need to loop some extra lines more, one line per secondary root
				    	// so we increase the loop counter limit by the number of secondary roots
				    	
				    	maxNbOfRoots = maxNbOfRoots + nbOfSecondaryRoots;
				    	
				    	// Get the length of the lateral or secondary root
				    	for (int k = 0; k < nbOfFields; k++ ){
				    		if (lineFields[k].contains("Length")){
				    			Double rootLength = Double.valueOf(lineFields[k+1].replace(",", "."));
				    			//System.out.println(rootLength);
				    			if (rootLength > minlateralrootlength){
				    				tempValue = rootLength;
				    			} else {
				    				tempValue = 0.00;
				    			}
				    		}
				    	}
				    	lateralRootsLenghSum = lateralRootsLenghSum + tempValue;
				    	//System.out.println(lateralRootsLenghSum);
				    }
				    
				    // Save the value for the current root
				    sumOfLatRootsLength[i]=lateralRootsLenghSum;
				    //System.out.println(roundDouble(sumOfLatRootsLength[i]));
		    		
				    // Skip 3 blank lines before the next root
				    dis.readLine();
				    dis.readLine();
				    dis.readLine();
		        }
			    
			    // write the output file		    
			    parsedAccession = writeFile(currentAccession,
			    							outputfilename,
			    		  				    experimentName,
			    		  				    boxName,
			    		  				    nbOfPlants,
			    		  				    mainRootLength,
			    		  				    nbOfLateralRoots,
			    		  				    sumOfLatRootsLength);

		    }
		    
		    fis.close();
		    bis.close();
		    dis.close();
		    
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return parsedAccession;
	}
	
	private static Accession writeFile(Accession parsedaccession,
			                           String outputfilename,
							 	  	   String experimentname,
							 	  	   String boxname,
							 	  	   int nbofplants,
							 	  	   Double[] mainrootlength,
							 	  	   int[] nboflateralroots,
							 	  	   Double[] sumoflatrootslength) throws IOException{
		
	    FileWriter f1 = new FileWriter(outputfilename);
	    
	    // Write first line with the columns titles
	    String source = "Experiment Name"+";"+
	    				"Box Name"+";"+
	    				"Nb of Plants"+";"+
	    				"Main Root Length"+";"+";"+
	    				"Nb of Lateral Roots"+";"+";"+
	    				"Sum of Lateral Roots Length"+"\r\n";
	    f1.write(source);
	    
	    // Write the second line, for this line we write the file name, the experiment name, 
	    // the box name and the number of plants. This will not repeated for the next plants.
	    source = experimentname+";"+
	    		 boxname+";"+
	    		 nbofplants+";"+
	    		 roundDouble(mainrootlength[0],"#.##")+";"+";"+
	    		 nboflateralroots[0]+";"+";"+
	    		 roundDouble(sumoflatrootslength[0],"#.##")+"\r\n";
	    
	    // Just to make sure the numbers are ok for Excel
	    String newSource = source.replace(".", ",");			    
	    f1.write(newSource);
	    
	    // we will now right the following lines based on the number of plants
	    for (int l = 1; l < nbofplants; l++ ){
	    	
	    	source = ";"+";"+";"+
					 roundDouble(mainrootlength[l],"#.##")+";"+";"+
					 nboflateralroots[l]+";"+";"+
					 roundDouble(sumoflatrootslength[l],"#.##")+"\r\n";
	    	
	    	newSource = source.replace(".", ",");    	
	    	f1.write(newSource);	    	
	    }
	    
	    // Calculate the means
	    Double mainRootLengthMean = meanDouble(mainrootlength);
	    parsedaccession.setMRLmean(mainRootLengthMean);
	    Double nbOfLateralRootsMean = meanInt(nboflateralroots);
	    parsedaccession.setNLRmean(nbOfLateralRootsMean);
	    Double sumOfLatRootsLengthMean = meanDouble(sumoflatrootslength);
	    parsedaccession.setSLRLmean(sumOfLatRootsLengthMean);
	    
	    // Calculate the standard deviations
	    Double mainRootLengthSD = sdDouble(mainrootlength);
	    parsedaccession.setMRLsd(mainRootLengthSD);
	    Double nbOfLateralRootsSD = sdInt(nboflateralroots);
	    parsedaccession.setNLRsd(nbOfLateralRootsSD);
	    Double sumOfLatRootsLengthSD = sdDouble(sumoflatrootslength);
	    parsedaccession.setSLRLsd(sumOfLatRootsLengthSD);
	    
	    // Calculate the standard errors
	    Double mainRootLengthSE = mainRootLengthSD/Math.sqrt(nbofplants-1);
	    parsedaccession.setMRLse(mainRootLengthSE);
	    Double nbOfLateralRootsSE = nbOfLateralRootsSD/Math.sqrt(nbofplants-1);
	    parsedaccession.setNLRse(nbOfLateralRootsSE);
	    Double sumOfLatRootsLengthSE = sumOfLatRootsLengthSD/Math.sqrt(nbofplants-1);
	    parsedaccession.setSLRLse(sumOfLatRootsLengthSE);
	    
	    // Write the line with the different mean values
	    source = ";"+";"+"Mean;"+
	    		 roundDouble(mainRootLengthMean,"#.##")+";"+";"+
	    		 roundDouble(nbOfLateralRootsMean,"#.##")+";"+";"+
	    		 roundDouble(sumOfLatRootsLengthMean,"#.##")+"\r\n";
	    newSource = source.replace(".", ",");
	    f1.write(newSource);

	    // Write the line with the different standard deviations
	    source = ";"+";"+"SD;"+
		 		 roundDouble(mainRootLengthSD,"#.##")+";"+";"+
		 		 roundDouble(nbOfLateralRootsSD,"#.##")+";"+";"+
		 		 roundDouble(sumOfLatRootsLengthSD,"#.##")+"\r\n";
	    newSource = source.replace(".", ",");
	    f1.write(newSource);

	    // Write the line with the different standard errors
	    source = ";"+";"+"SE;"+
		 		 roundDouble(mainRootLengthSE,"#.##")+";"+";"+
		 		 roundDouble(nbOfLateralRootsSE,"#.##")+";"+";"+
		 		 roundDouble(sumOfLatRootsLengthSE,"#.##")+"\r\n";
	    newSource = source.replace(".", ",");
	    f1.write(newSource);

	    f1.close();
	    
	    return parsedaccession;
	}

	private static Double[] writeFile2(String outputfilename,
	 	  	                  List<Accession> accessionlist) throws IOException{
		
		FileWriter f1 = new FileWriter(outputfilename);
		
		// Write first line with the columns titles
		String source = "Experiment Name"+";"+
						"Accession"+";"+
						"Concentration"+";"+
						"Box Name"+";"+
						""+";"+
						"Main Root Length"+";"+
						"Nb of Lateral Roots"+";"+
						"Sum of Lateral Roots Length"+"\r\n";
		
		f1.write(source);

		// Write the file lines
		for (int i=0; i<accessionlist.size(); i++) {
			
			source = accessionlist.get(i).getExperimentName()+";"+
					 accessionlist.get(i).getAccessionName()+";"+
					 accessionlist.get(i).getConcentration()+";"+
					 accessionlist.get(i).getBox()+";"+
					 ""+";"+
					 roundDouble(accessionlist.get(i).getMRLmean(),"#.##")+";"+
					 roundDouble(accessionlist.get(i).getNLRmean(),"#.##")+";"+
					 roundDouble(accessionlist.get(i).getSLRLmean(),"#.##")+"\r\n";

//			 accessionlist.get(i).getNbOfPlants()+";"+

			
			// Just to make sure the numbers are ok for Excel
			String newSource = source.replace(".", ",");			    
			f1.write(newSource);
		}

		Double[] globalMeans = new Double[3];			   
		globalMeans = calculateGlobalMeans(accessionlist);
		
		// Write a blank line
		source = ""+";"+""+";"+""+";"+""+";"+""+";"+""+";"+""+";"+""+"\r\n";
		f1.write(source);
		
		// Write the line with the global means
		source = ""+";"+
				 ""+";"+
				 ""+";"+
				 ""+";"+
				 "Mean"+";"+
				 roundDouble(globalMeans[0],"#.##")+";"+
				 roundDouble(globalMeans[1],"#.##")+";"+
				 roundDouble(globalMeans[2],"#.##")+"\r\n";
		
		// Just to make sure the numbers are ok for Excel
		String newSource = source.replace(".", ",");
		f1.write(newSource);
		
		f1.close();
		return globalMeans;
	}

	private static void writeFilesPerConcentration(String outputdir,
												   String concentration,
												   List<String> accessionnames,
												   List<Accession> accessionlist) throws IOException{
		
		String outputfilename1 = outputdir+"Accessions_02_"+concentration+".csv";
		String outputfilename2 = outputdir+"Accessions_03_"+concentration+".csv";
		
		FileWriter f1 = new FileWriter(outputfilename1);
		FileWriter f2 = new FileWriter(outputfilename2);
						
		// Write first line with the columns titles for the first file
		String source = "Experiment Name"+";"+
						"Accession"+";"+
						"Concentration"+";"+
						"Box Name"+";"+
						""+";"+
						"Main Root Length"+";"+
						"Nb of Lateral Roots"+";"+
						"Sum of Lateral Roots Length"+"\r\n";
		
		f1.write(source);
		
		// Write first line with the columns titles for the second file
		source = "Accession"+";"+
				 "Concentration"+";"+
				 "Main Root Length"+";"+";"+";"+
				 "Nb of Lateral Roots"+";"+";"+";"+
				 "Sum of Lateral Roots Length"+";"+";"+";"+"\r\n";
		
		f2.write(source);

		
		for (int i=0; i<accessionnames.size(); i++) {

			Double MRLmeanA = 0.00,MRLmeanB = 0.00,MRLmeanC = 0.00,MRLmeanD = 0.00;
			Double NLRmeanA = 0.00,NLRmeanB = 0.00,NLRmeanC = 0.00,NLRmeanD = 0.00;
			Double SLRLmeanA = 0.00,SLRLmeanB = 0.00,SLRLmeanC = 0.00,SLRLmeanD = 0.00;
			
			for (int j=0; j<accessionlist.size(); j++) {
				
				if ((accessionnames.get(i).equals(accessionlist.get(j).getAccessionName())) &
					(accessionlist.get(j).getConcentration().equals(concentration)) &
					(accessionlist.get(j).getBox().equals("A"))) {
					source = accessionlist.get(j).getExperimentName()+";"+
					 		 accessionlist.get(j).getAccessionName()+";"+
					 		 accessionlist.get(j).getConcentration()+";"+
					 		 accessionlist.get(j).getBox()+";"+
					 		 ""+";"+
					 		 roundDouble(accessionlist.get(j).getMRLmean(),"#.##")+";"+
					 		 roundDouble(accessionlist.get(j).getNLRmean(),"#.##")+";"+
					 		 roundDouble(accessionlist.get(j).getSLRLmean(),"#.##")+"\r\n";

//			 		 accessionlist.get(j).getNbOfPlants()+";"+
					
					String newSource = source.replace(".", ",");			    
					f1.write(newSource);
			
					MRLmeanA = roundDouble(accessionlist.get(j).getMRLmean(),"#.##");
					NLRmeanA = roundDouble(accessionlist.get(j).getNLRmean(),"#.##");
					SLRLmeanA = roundDouble(accessionlist.get(j).getSLRLmean(),"#.##");
					
					//System.out.println(MRLmeanA+" "+NLRmeanA+" "+SLRLmeanA);
				}
			}	
		
			for (int j=0; j<accessionlist.size(); j++) {
				
				if ((accessionnames.get(i).equals(accessionlist.get(j).getAccessionName())) &
				    (accessionlist.get(j).getConcentration().equals(concentration)) &
					(accessionlist.get(j).getBox().equals("B"))) {
					source = accessionlist.get(j).getExperimentName()+";"+
							 accessionlist.get(j).getAccessionName()+";"+
							 accessionlist.get(j).getConcentration()+";"+
							 accessionlist.get(j).getBox()+";"+
							 ""+";"+
							 roundDouble(accessionlist.get(j).getMRLmean(),"#.##")+";"+
							 roundDouble(accessionlist.get(j).getNLRmean(),"#.##")+";"+
							 roundDouble(accessionlist.get(j).getSLRLmean(),"#.##")+"\r\n";
							
					String newSource = source.replace(".", ",");			    
					f1.write(newSource);	

					MRLmeanB = roundDouble(accessionlist.get(j).getMRLmean(),"#.##");
					NLRmeanB = roundDouble(accessionlist.get(j).getNLRmean(),"#.##");
					SLRLmeanB = roundDouble(accessionlist.get(j).getSLRLmean(),"#.##");

					//System.out.println(MRLmeanB+" "+NLRmeanB+" "+SLRLmeanB);
					
				}
			}	

			for (int j=0; j<accessionlist.size(); j++) {
				
				if ((accessionnames.get(i).equals(accessionlist.get(j).getAccessionName())) &
					(accessionlist.get(j).getConcentration().equals(concentration)) &
					(accessionlist.get(j).getBox().equals("C"))) {
					source = accessionlist.get(j).getExperimentName()+";"+
							 accessionlist.get(j).getAccessionName()+";"+
							 accessionlist.get(j).getConcentration()+";"+
							 accessionlist.get(j).getBox()+";"+
							 ""+";"+
							 roundDouble(accessionlist.get(j).getMRLmean(),"#.##")+";"+
							 roundDouble(accessionlist.get(j).getNLRmean(),"#.##")+";"+
							 roundDouble(accessionlist.get(j).getSLRLmean(),"#.##")+"\r\n";
									
					String newSource = source.replace(".", ",");			    
					f1.write(newSource);

					MRLmeanC = roundDouble(accessionlist.get(j).getMRLmean(),"#.##");
					NLRmeanC = roundDouble(accessionlist.get(j).getNLRmean(),"#.##");
					SLRLmeanC = roundDouble(accessionlist.get(j).getSLRLmean(),"#.##");
					
					//System.out.println(MRLmeanC+" "+NLRmeanC+" "+SLRLmeanC);

				}	
			}

			for (int j=0; j<accessionlist.size(); j++) {
				
				if ((accessionnames.get(i).equals(accessionlist.get(j).getAccessionName())) &
					(accessionlist.get(j).getConcentration().equals(concentration)) &
					(accessionlist.get(j).getBox().equals("D"))) {
					source = accessionlist.get(j).getExperimentName()+";"+
							 accessionlist.get(j).getAccessionName()+";"+
							 accessionlist.get(j).getConcentration()+";"+
							 accessionlist.get(j).getBox()+";"+
							 ""+";"+
							 roundDouble(accessionlist.get(j).getMRLmean(),"#.##")+";"+
							 roundDouble(accessionlist.get(j).getNLRmean(),"#.##")+";"+
							 roundDouble(accessionlist.get(j).getSLRLmean(),"#.##")+"\r\n";
									
					String newSource = source.replace(".", ",");			    
					f1.write(newSource);

					MRLmeanC = roundDouble(accessionlist.get(j).getMRLmean(),"#.##");
					NLRmeanC = roundDouble(accessionlist.get(j).getNLRmean(),"#.##");
					SLRLmeanC = roundDouble(accessionlist.get(j).getSLRLmean(),"#.##");
					
					//System.out.println(MRLmeanD+" "+NLRmeanD+" "+SLRLmeanD);

				}	
			}
			
//			System.out.println(MRLmeanA+" "+NLRmeanA+" "+SLRLmeanA);
//			System.out.println(MRLmeanB+" "+NLRmeanB+" "+SLRLmeanB);
//			System.out.println(MRLmeanC+" "+NLRmeanC+" "+SLRLmeanC);
//			System.out.println(MRLmeanD+" "+NLRmeanD+" "+SLRLmeanD);
//			System.out.println("---");

			Double[] array1 = moveToArray(MRLmeanA,MRLmeanB,MRLmeanC,MRLmeanD);		
			Double[] array2 = moveToArray(NLRmeanA,NLRmeanB,NLRmeanC,NLRmeanD);
			Double[] array3 = moveToArray(SLRLmeanA,SLRLmeanB,SLRLmeanC,SLRLmeanD);

			if (array1.length != 0){
				
				// calculate the different mean values for accessions A,B,C,D
				Double MRLmean,NLRmean,SLRLmean;
				
				if (array1.length == 1){
					MRLmean = array1[0];
				} else {
					MRLmean = roundDouble(meanDouble(array1),"#.##");
				}

				if (array2.length == 1){
					NLRmean = array2[0];
				} else {
					NLRmean = roundDouble(meanDouble(array2),"#.##");
				}

				if (array3.length == 1){
					SLRLmean = array3[0];
				} else {
					SLRLmean = roundDouble(meanDouble(array3),"#.##");
				}
				
				// Write the Mean values in the file
				source = ""+";"+
		         		 ""+";"+
		         		 ""+";"+
		         		 ""+";"+
		         		 "Mean"+";"+
		         		 roundDouble(MRLmean,"#.##")+";"+
		         		 roundDouble(NLRmean,"#.##")+";"+
		         		 roundDouble(SLRLmean,"#.##")+"\r\n";
				
				String newSource = source.replace(".", ",");			    
				f1.write(newSource);
				
				// calculate the different SD values for accessions A,B,C,D
				Double MRLsd,NLRsd,SLRLsd;
				
				if (array1.length == 1){
					MRLsd = 0.00;
				} else {
					MRLsd = roundDouble(sdDouble(array1),"#.##");
				}

				if (array2.length == 1){
					NLRsd = 0.00;
				} else {
					NLRsd = roundDouble(sdDouble(array2),"#.##");
				}

				if (array3.length == 1){
					SLRLsd = 0.00;
				} else {
					SLRLsd = roundDouble(sdDouble(array3),"#.##");
				}

				// Write the SD values in the file
				source = ""+";"+
	    		 		 ""+";"+
	    		 		 ""+";"+
	    		 		 ""+";"+
	    		 		 "SD"+";"+
	    		 		 roundDouble(MRLsd,"#.##")+";"+
	    		 		 roundDouble(NLRsd,"#.##")+";"+
	    		 		 roundDouble(SLRLsd,"#.##")+"\r\n";
		
				newSource = source.replace(".", ",");			    
				f1.write(newSource);

				// calculate the different SE values for accessions A,B,C,D
				Double MRLse,NLRse,SLRLse;
				
				if (array1.length == 1){
					MRLse = 0.00;
				} else {
					MRLse = roundDouble((MRLsd/Math.sqrt(array1.length-1)),"#.##");
				}

				if (array2.length == 1){
					NLRse = 0.00;
				} else {
					NLRse = roundDouble((NLRsd/Math.sqrt(array2.length-1)),"#.##");
				}

				if (array3.length == 1){
					SLRLse = 0.00;
				} else {
					SLRLse = roundDouble((SLRLsd/Math.sqrt(array3.length-1)),"#.##");
				}

				// Write the SE values in the file
				
				source = ""+";"+
					     ""+";"+
					     ""+";"+
					     ""+";"+
					     "SE"+";"+
					     roundDouble(MRLse,"#.##")+";"+
					     roundDouble(NLRse,"#.##")+";"+
					     roundDouble(SLRLse,"#.##")+"\r\n";

				newSource = source.replace(".", ",");			    
				f1.write(newSource);
				
				// Write the data in the second file

				source = accessionnames.get(i)+";"+
						 concentration+";"+
						 roundDouble(MRLmean,"#.##")+";"+"±"+";"+roundDouble(MRLse,"#.##")+";"+
						 roundDouble(NLRmean,"#.##")+";"+"±"+";"+roundDouble(NLRse,"#.##")+";"+
						 roundDouble(SLRLmean,"#.##")+";"+"±"+";"+roundDouble(SLRLse,"#.##")+";"+"\r\n";
				newSource = source.replace(".", ",");			    
				f2.write(newSource);
								
			}
		}		
		f1.close();
		f2.close();
	}
	
	private static void writeCorrectedFilesPerConcentration(String outputdir,
			   									   			String concentration,
			   									   			List<String> accessionnames,
			   									   			List<Accession> accessionlist,
			   									   			Double[] globalmeans) throws IOException{
		
		
		// This routine writes a file with the corrected accessions values for a specific concentration
		
		String outputfilename1 = outputdir+"Accessions_02_"+concentration+"_corrected.csv";
		String outputfilename2 = outputdir+"Accessions_03_"+concentration+"_corrected.csv";

		FileWriter f1 = new FileWriter(outputfilename1);
		FileWriter f2 = new FileWriter(outputfilename2);

		// Write first line with the columns titles for the first file
		String source = "Experiment Name"+";"+
						"Accession"+";"+
						"Concentration"+";"+
						"Box Name"+";"+
						""+";"+
						"Main Root Length"+";"+
						"Nb of Lateral Roots"+";"+
						"Sum of Lateral Roots Length"+"\r\n";
		f1.write(source);

		// Write first line with the columns titles for the second file
		source = "Accession"+";"+
				 "Concentration"+";"+
				 "Main Root Length"+";"+";"+";"+
				 "Nb of Lateral Roots"+";"+";"+";"+
				 "Sum of Lateral Roots Length"+";"+";"+";"+"\r\n";
		f2.write(source);

		for (int i=0; i<accessionnames.size(); i++) {

			Double MRLmeanA = 0.00,MRLmeanB = 0.00,MRLmeanC = 0.00,MRLmeanD = 0.00;
			Double NLRmeanA = 0.00,NLRmeanB = 0.00,NLRmeanC = 0.00,NLRmeanD = 0.00;
			Double SLRLmeanA = 0.00,SLRLmeanB = 0.00,SLRLmeanC = 0.00,SLRLmeanD = 0.00;

			for (int j=0; j<accessionlist.size(); j++) {
				
				// Get the corrected data for box A
				if ((accessionnames.get(i).equals(accessionlist.get(j).getAccessionName())) &
					(accessionlist.get(j).getConcentration().equals(concentration)) &
					(accessionlist.get(j).getBox().equals("A"))) {
					
					Double MRLmeanCorrected = (accessionlist.get(j).getMRLmean()/globalmeans[0])-1;
					Double NLRmeanCorrected = (accessionlist.get(j).getNLRmean()/globalmeans[1])-1;
					Double SLRLmeanCorrected = (accessionlist.get(j).getSLRLmean()/globalmeans[2])-1;
					
					source = accessionlist.get(j).getExperimentName()+";"+
							 accessionlist.get(j).getAccessionName()+";"+
							 accessionlist.get(j).getConcentration()+";"+
							 accessionlist.get(j).getBox()+";"+
							 ""+";"+
							 roundDouble(MRLmeanCorrected,"#.##")+";"+
							 roundDouble(NLRmeanCorrected,"#.##")+";"+
							 roundDouble(SLRLmeanCorrected,"#.##")+"\r\n";

//					 accessionlist.get(j).getNbOfPlants()+";"+

					String newSource = source.replace(".", ",");			    
					f1.write(newSource);

					MRLmeanA = roundDouble(MRLmeanCorrected,"#.##");
					NLRmeanA = roundDouble(NLRmeanCorrected,"#.##");
					SLRLmeanA = roundDouble(SLRLmeanCorrected,"#.##");

					//System.out.println(MRLmeanA+" "+NLRmeanA+" "+SLRLmeanA);
				}
			}	

			for (int j=0; j<accessionlist.size(); j++) {

				// Get the corrected data for box B
				if ((accessionnames.get(i).equals(accessionlist.get(j).getAccessionName())) &
					(accessionlist.get(j).getConcentration().equals(concentration)) &
					(accessionlist.get(j).getBox().equals("B"))) {

					Double MRLmeanCorrected = (accessionlist.get(j).getMRLmean()/globalmeans[0])-1;
					Double NLRmeanCorrected = (accessionlist.get(j).getNLRmean()/globalmeans[1])-1;
					Double SLRLmeanCorrected = (accessionlist.get(j).getSLRLmean()/globalmeans[2])-1;

					source = accessionlist.get(j).getExperimentName()+";"+
							 accessionlist.get(j).getAccessionName()+";"+
							 accessionlist.get(j).getConcentration()+";"+
							 accessionlist.get(j).getBox()+";"+
							 ""+";"+
							 roundDouble(MRLmeanCorrected,"#.##")+";"+
							 roundDouble(NLRmeanCorrected,"#.##")+";"+
							 roundDouble(SLRLmeanCorrected,"#.##")+"\r\n";

					String newSource = source.replace(".", ",");			    
					f1.write(newSource);	

					MRLmeanB = roundDouble(MRLmeanCorrected,"#.##");
					NLRmeanB = roundDouble(NLRmeanCorrected,"#.##");
					SLRLmeanB = roundDouble(SLRLmeanCorrected,"#.##");

					//System.out.println(MRLmeanB+" "+NLRmeanB+" "+SLRLmeanB);

				}
			}	

			for (int j=0; j<accessionlist.size(); j++) {

				// Get the corrected data for box C
				if ((accessionnames.get(i).equals(accessionlist.get(j).getAccessionName())) &
					(accessionlist.get(j).getConcentration().equals(concentration)) &
					(accessionlist.get(j).getBox().equals("C"))) {

					Double MRLmeanCorrected = (accessionlist.get(j).getMRLmean()/globalmeans[0])-1;
					Double NLRmeanCorrected = (accessionlist.get(j).getNLRmean()/globalmeans[1])-1;
					Double SLRLmeanCorrected = (accessionlist.get(j).getSLRLmean()/globalmeans[2])-1;

					source = accessionlist.get(j).getExperimentName()+";"+
							 accessionlist.get(j).getAccessionName()+";"+
							 accessionlist.get(j).getConcentration()+";"+
							 accessionlist.get(j).getBox()+";"+
							 ""+";"+
							 roundDouble(MRLmeanCorrected,"#.##")+";"+
							 roundDouble(NLRmeanCorrected,"#.##")+";"+
							 roundDouble(SLRLmeanCorrected,"#.##")+"\r\n";

					String newSource = source.replace(".", ",");			    
					f1.write(newSource);

					MRLmeanC = roundDouble(MRLmeanCorrected,"#.##");
					NLRmeanC = roundDouble(NLRmeanCorrected,"#.##");
					SLRLmeanC = roundDouble(SLRLmeanCorrected,"#.##");

					//System.out.println(MRLmeanC+" "+NLRmeanC+" "+SLRLmeanC);
				}	
			}

			for (int j=0; j<accessionlist.size(); j++) {

				// Get the corrected data for box D
				if ((accessionnames.get(i).equals(accessionlist.get(j).getAccessionName())) &
					(accessionlist.get(j).getConcentration().equals(concentration)) &
					(accessionlist.get(j).getBox().equals("D"))) {

					Double MRLmeanCorrected = (accessionlist.get(j).getMRLmean()/globalmeans[0])-1;
					Double NLRmeanCorrected = (accessionlist.get(j).getNLRmean()/globalmeans[1])-1;
					Double SLRLmeanCorrected = (accessionlist.get(j).getSLRLmean()/globalmeans[2])-1;

					source = accessionlist.get(j).getExperimentName()+";"+
							 accessionlist.get(j).getAccessionName()+";"+
							 accessionlist.get(j).getConcentration()+";"+
							 accessionlist.get(j).getBox()+";"+
							 ""+";"+
							 roundDouble(MRLmeanCorrected,"#.##")+";"+
							 roundDouble(NLRmeanCorrected,"#.##")+";"+
							 roundDouble(SLRLmeanCorrected,"#.##")+"\r\n";

					String newSource = source.replace(".", ",");			    
					f1.write(newSource);

					MRLmeanC = roundDouble(MRLmeanCorrected,"#.##");
					NLRmeanC = roundDouble(NLRmeanCorrected,"#.##");
					SLRLmeanC = roundDouble(SLRLmeanCorrected,"#.##");

					//System.out.println(MRLmeanD+" "+NLRmeanD+" "+SLRLmeanD);
				}	
			}
			
			
			//System.out.println(MRLmeanA+" "+NLRmeanA+" "+SLRLmeanA);
			//System.out.println(MRLmeanB+" "+NLRmeanB+" "+SLRLmeanB);
			//System.out.println(MRLmeanC+" "+NLRmeanC+" "+SLRLmeanC);
			//System.out.println(MRLmeanD+" "+NLRmeanD+" "+SLRLmeanD);			
			//System.out.println("---");

			// move all the means to a specific array
			Double[] array1 = moveToArray(MRLmeanA,MRLmeanB,MRLmeanC,MRLmeanD);		
			Double[] array2 = moveToArray(NLRmeanA,NLRmeanB,NLRmeanC,NLRmeanD);
			Double[] array3 = moveToArray(SLRLmeanA,SLRLmeanB,SLRLmeanC,SLRLmeanD);
			
			//System.out.println(array1.length);
			//System.out.println(array2.length);
			//System.out.println(array3.length);			
			//System.out.println("---");
			
			Double MRLmean,NLRmean,SLRLmean;
			
			// Calculate MRLmean
			if (array1.length != 0){
				if (array1.length == 1){
					MRLmean = array1[0];
				} else {
					MRLmean = roundDouble(meanDouble(array1),"#.##");
				}
			} else {
				MRLmean = 0.00;
			}

			// Calculate NLRmean
			if (array2.length != 0){
				if (array2.length == 1){
					NLRmean = array2[0];
				} else {
					NLRmean = roundDouble(meanDouble(array2),"#.##");				
				}
			} else {
				NLRmean = 0.00;
			}			

			// Calculate SLRLmean
			if (array3.length != 0){
				if (array3.length == 1){
					SLRLmean = array3[0];
				} else {
					SLRLmean = roundDouble(meanDouble(array3),"#.##");
				}
			} else {
				SLRLmean = 0.00;
			}
			
			// Write the Mean values in the file
			source = ""+";"+
					 ""+";"+
					 ""+";"+
					 ""+";"+
					 "Mean"+";"+
					 roundDouble(MRLmean,"#.##")+";"+
					 roundDouble(NLRmean,"#.##")+";"+
					 roundDouble(SLRLmean,"#.##")+"\r\n";

			String newSource = source.replace(".", ",");			    
			f1.write(newSource);

			// calculate the different SD values for accessions A,B,C
			Double MRLsd,NLRsd,SLRLsd;

			if (array1.length == 1 | array1.length == 0){
					MRLsd = 0.00;
			} else {
				MRLsd = roundDouble(sdDouble(array1),"#.##");
			}

			if (array2.length == 1 | array2.length == 0){
				NLRsd = 0.00;
			} else {
				NLRsd = roundDouble(sdDouble(array2),"#.##");
			}

			if (array3.length == 1 | array3.length == 0){
				SLRLsd = 0.00;
			} else {
				SLRLsd = roundDouble(sdDouble(array3),"#.##");
			}

			// Write the SD values in the file
			source = ""+";"+
					 ""+";"+
					 ""+";"+
					 ""+";"+
					 "SD"+";"+
					 roundDouble(MRLsd,"#.##")+";"+
					 roundDouble(NLRsd,"#.##")+";"+
					 roundDouble(SLRLsd,"#.##")+"\r\n";

			newSource = source.replace(".", ",");			    
			f1.write(newSource);

			// calculate the different SE values for accessions A,B,C
			Double MRLse,NLRse,SLRLse;

			if (array1.length == 1 | array1.length == 0){
				MRLse = 0.00;
			} else {
				MRLse = roundDouble((MRLsd/Math.sqrt(array1.length-1)),"#.##");
			}

			if (array2.length == 1 | array2.length == 0){
				NLRse = 0.00;
			} else {
				NLRse = roundDouble((NLRsd/Math.sqrt(array2.length-1)),"#.##");
			}

			if (array3.length == 1 | array3.length == 0){
				SLRLse = 0.00;
			} else {
				SLRLse = roundDouble((SLRLsd/Math.sqrt(array3.length-1)),"#.##");
			}

			// Write the SE values in the file

			source = ""+";"+
					 ""+";"+
					 ""+";"+
					 ""+";"+
					 "SE"+";"+
					 roundDouble(MRLse,"#.##")+";"+
					 roundDouble(NLRse,"#.##")+";"+
					 roundDouble(SLRLse,"#.##")+"\r\n";

			newSource = source.replace(".", ",");			    
			f1.write(newSource);

			// Write the data in the second file

			source = accessionnames.get(i)+";"+
					 concentration+";"+
					 roundDouble(MRLmean,"#.##")+";"+"±"+";"+roundDouble(MRLse,"#.##")+";"+
					 roundDouble(NLRmean,"#.##")+";"+"±"+";"+roundDouble(NLRse,"#.##")+";"+
					 roundDouble(SLRLmean,"#.##")+";"+"±"+";"+roundDouble(SLRLse,"#.##")+";"+"\r\n";
			newSource = source.replace(".", ",");			    
			f2.write(newSource);

		}		
		f1.close();
		f2.close();
	}
	
	
    private static String getStringLineItem(String line, int index, String patternstr) {
    	
    	// This routine takes a string line as input and returns a string based on the index value 	
    	
    	String fieldStr;
    	String[] fields = line.split(patternstr);
    	if (fields.length==1) {
    		fieldStr = "";
    	} else {
    		fieldStr = fields[index];
    	}
    	return fieldStr;
    }
    
    private static int getIntegerLineItem(String line, int index, String patternstr) {
 
    	// This routine takes a string line as input and returns an integer based on the index value
   	
    	String[] fields = line.split(patternstr);
    	return Integer.parseInt(fields[index]);
    }
    
    private static Double getDoubleLineItem(String line, int index, String patternstr) {

    	// This routine takes a string line as input and returns a double based on the index value

    	String[] fields = line.split(patternstr);
    	Double value = Double.valueOf(fields[index].replace(",", "."));
    	return value;
    }
    
    private static String[] getFields(String line, String patternstr) {
    	
    	// This routine takes a string line as input and returns an array of string based on the split pattern
    	String[] fields = line.split(patternstr);
    	return fields;
    }
    
    static Double roundDouble(Double d, String decimalformat) {
    	
    	// This routine takes a double as input an returns a rounded double based on the format
    	
    	DecimalFormat twoDForm = new DecimalFormat(decimalformat);
	return Double.valueOf(twoDForm.format(d).replace(",", "."));
    }
    
    static Double[] calculateGlobalMeans(List<Accession> list) {
    	
    	// This routines calculates the global means
    	// It retrieves for each accession the MRLmean, NRLmean, SLRLmean value
    	// It then calculate for each of them the global mean value and return them as an array
    	
    	Double[] calculatedMeans = new Double[3];
    	Double[] MRLmeans = new Double[list.size()];
    	Double[] NLRmeans = new Double[list.size()];
    	Double[] SLRLmeans = new Double[list.size()];
    	
    	for (int i=0; i<list.size(); i++) {
    		MRLmeans[i] = roundDouble(list.get(i).getMRLmean(),"#.##");
    		NLRmeans[i] = roundDouble(list.get(i).getNLRmean(),"#.##");
    		SLRLmeans[i] = roundDouble(list.get(i).getSLRLmean(),"#.##");
        }
    	
    	calculatedMeans[0] = roundDouble(meanDouble(MRLmeans),"#.##");
    	calculatedMeans[1] = roundDouble(meanDouble(NLRmeans),"#.##");
    	calculatedMeans[2] = roundDouble(meanDouble(SLRLmeans),"#.##");    	
    	
        return calculatedMeans;
    }
    
    static Double meanDouble(Double[] p) {

    	// This routine returns the mean for doubles

    	Double sum = 0.00;  // sum of all the elements
        for (int i=0; i<p.length; i++) {
            sum += p[i];
        }
        return sum / p.length;
    }
    
    static Double meanInt(int[] p) {
    	
    	// This routine returns the mean for integers
    	
        Double sum = 0.00;  // sum of all the elements
        for (int i=0; i<p.length; i++) {
            sum += p[i];
        }
        return sum / p.length;
    }
    
    public static Double sdDouble ( Double[] data )
    {
    // This routine returns the standard deviation for doubles
    // sd is sqrt of sum of (values-mean) squared divided by n - 1
    	
    // Calculate the mean
    Double mean = 0.00;
    final int n = data.length;
    if ( n < 2 )
       {
       return Double.NaN;
       }
    for ( int i=0; i<n; i++ )
       {
       mean += data[i];
       }
    mean /= n;

    // calculate the sum of squares
    Double sum = 0.00;
    for ( int i=0; i<n; i++ )
       {
       final Double v = data[i] - mean;
       sum += v * v;
       }

    // Change to ( n - 1 ) to n if you have complete data instead of a sample.
    return Math.sqrt( sum / ( n - 1 ) );
    }

    public static Double sdInt ( int[] data )
    {
    // This routine returns the standard deviation for integers	
    // sd is sqrt of sum of (values-mean) squared divided by n - 1
    	
    // Calculate the mean
    Double mean = 0.00;
    final int n = data.length;
    if ( n < 2 )
       {
       return Double.NaN;
       }
    for ( int i=0; i<n; i++ )
       {
       mean += data[i];
       }
    mean /= n;
    
    // calculate the sum of squares
    Double sum = 0.00;
    for ( int i=0; i<n; i++ )
       {
       final Double v = data[i] - mean;
       sum += v * v;
       }
    
    // Change to ( n - 1 ) to n if you have complete data instead of a sample.
    return Math.sqrt( sum / ( n - 1 ) );
    }

    static List<String> getUniqueAccessionsNames(List<Accession> list) {
    	
    	// This routines returns a list with unique accession names
    	
    	List<String> uniqueNames = new ArrayList<String>();
    
    	String currentName = list.get(0).getAccessionName();
    	uniqueNames.add(currentName);
    	
    	for (int i=1; i<list.size(); i++) {
    		if (!currentName.equals(list.get(i).getAccessionName())) {
    			currentName = list.get(i).getAccessionName();
    			uniqueNames.add(currentName);
    		}
    	}	    	    	
        return uniqueNames;
    }    

    static Double[] moveToArray(Double value1, Double value2, Double value3, Double value4) {
    	
    	// Moves the 4 Doubles received as input into one array
    	// If a value is equal to zero then it is not added in the array
    	// This means the routine can return an array of size 0 !
    	
    	List<Double> myList = new ArrayList<Double>();
    	
    	if (value1 != 0) {
    		myList.add(value1);
    	}
    	if (value2 != 0) {
    		myList.add(value2);
    	}
    	if (value3 != 0) {
    		myList.add(value3);
    	}
    	if (value4 != 0) {
    		myList.add(value4);
    	}

    	Double [] myArray = new Double[myList.size()];
    	
    	for (int i=0;i<myList.size();i++){
    		myArray[i]=myList.get(i);
    	}
    	
        return myArray;
    }
    

}

