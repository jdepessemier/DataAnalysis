public class Accession {
	
	private String experimentName;
	private String accessionName;
	private String concentration;
	private String box;
	private String boxName;
	private int nbOfPlants;
	
	// Main Root Length (MRL) mean, standard deviation, and standard error
	private Double MRLmean;
	private Double MRLsd;
	private Double MRLse;
	
	// Number of Lateral Roots (NLR) mean, standard deviation, and standard error 
	private Double NLRmean;
	private Double NLRsd;
	private Double NLRse;
	
	// Sum of Lateral Roots Length (SLRL) mean, standard deviation, and standard error
	private Double SLRLmean;
	private Double SLRLsd;
	private Double SLRLse;

	public Accession() {
		experimentName = "";
		accessionName = "";
		concentration = "";
		box = "";
		boxName = "";
		nbOfPlants = 0;
		MRLmean = 0.00;
		MRLsd = 0.00;
		MRLse = 0.00;
		NLRmean = 0.00;
		NLRsd = 0.00;
		NLRse = 0.00;
		SLRLmean = 0.00;
		SLRLsd = 0.00;
		SLRLse = 0.00;		
	}
	
	public Accession(String experimentname,
					 String accessionname,
					 String concentration,
					 String box,
					 String boxname,
					 int nbofplants,
					 Double mainrootlengthmean,
					 Double mainrootlengthsd,
					 Double mainrootlengthse,
					 Double nboflateralrootsmean,
					 Double nboflateralrootssd,
					 Double nboflateralrootsse,
					 Double sumoflateralrootslengthmean,
					 Double sumoflateralrootslengthsd,
					 Double sumoflateralrootslengthse) {
		this.experimentName = experimentname;
		this.accessionName = accessionname;
		this.concentration = concentration;
		this.box = box;
		this.boxName = boxname;
		this.nbOfPlants = nbofplants;
		this.MRLmean = mainrootlengthmean;
		this.MRLsd = mainrootlengthsd;
		this.MRLse = mainrootlengthse;
		this.NLRmean = nboflateralrootsmean;
		this.NLRsd = nboflateralrootssd;
		this.NLRse = nboflateralrootsse;
		this.SLRLmean = sumoflateralrootslengthmean;
		this.SLRLsd = sumoflateralrootslengthsd;
		this.SLRLse = sumoflateralrootslengthse;
	}

	// Experiment Name
	
	public String getExperimentName() {
		return experimentName;
	}

	public void setExperimentName(String value) {
		experimentName = value;
	}

	// Accession Name
	
	public String getAccessionName() {
		return accessionName;
	}

	public void setAccessionName(String value) {
		accessionName = value;
	}

	// Concentration
	
	public String getConcentration() {
		return concentration;
	}

	public void setConcentration(String value) {
		concentration = value;
	}

	// Box
	
	public String getBox() {
		return box;
	}

	public void setBox(String value) {
		box = value;
	}

	// Box Name
	
	public String getBoxName() {
		return boxName;
	}

	public void setBoxName(String value) {
		boxName = value;
	}

	// Number of Plants
	
	public int getNbOfPlants() {
		return nbOfPlants;
	}

	public void setNbOfPlants(int value) {
		nbOfPlants = value;
	}

	// Main Root Length
	
	public Double getMRLmean() {
		return MRLmean;
	}

	public void setMRLmean(Double value) {
		MRLmean = value;
	}

	public Double getMRLsd() {
		return MRLsd;
	}

	public void setMRLsd(Double value) {
		MRLsd = value;
	}

	public Double getMRLse() {
		return MRLse;
	}

	public void setMRLse(Double value) {
		MRLse = value;
	}

	// Number of Lateral Roots
	
	public Double getNLRmean() {
		return NLRmean;
	}

	public void setNLRmean(Double value) {
		NLRmean = value;
	}

	public Double getNLRsd() {
		return NLRsd;
	}

	public void setNLRsd(Double value) {
		NLRsd = value;
	}

	public Double getNLRse() {
		return NLRse;
	}

	public void setNLRse(Double value) {
		NLRse = value;
	}

	// Sum of Lateral Roots Length
	
	public Double getSLRLmean() {
		return SLRLmean;
	}

	public void setSLRLmean(Double value) {
		SLRLmean = value;
	}

	public Double getSLRLsd() {
		return SLRLsd;
	}

	public void setSLRLsd(Double value) {
		SLRLsd = value;
	}

	public Double getSLRLse() {
		return SLRLse;
	}

	public void setSLRLse(Double value) {
		SLRLse = value;
	}

	
}
	
