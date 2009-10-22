package org.odk.collect.android.functions;

import java.util.Vector;

import org.javarosa.core.model.condition.IFunctionHandler;
import org.odk.collect.android.database.HCTDbAdapter;
import org.odk.collect.android.logic.HCTSharedConstants;

/**
 * Looks for a "unique" call in XForm and return true or false for a unique entry
 * 
 * @author Samuel Mbugua (sthaiya@gmail.com)
 */

public class UniqueFunction implements IFunctionHandler {
	private static HCTDbAdapter mDbAdapter;
	
	public Object eval(Object[] args) {

		String fieldName = (String) args[0];
		String fieldValue = (String) args[1];

		return confirmNewID(fieldName, fieldValue);
	}

	public String getName() {
		return "unique";
	}

	@SuppressWarnings("unchecked")
	public Vector getPrototypes() {

		Class[] prototypes = { String.class, String.class };
		Vector v = new Vector();
		v.add(prototypes);
		return v;
	}

	public boolean rawArgs() {
		// Auto-generated method stub
		return false;
	}

	public boolean realTime() {
		// Auto-generated method stub
		return false;
	}
	
	private boolean confirmNewIndividual(String idType, String id){
		boolean unique=false;
		String fullId=idType + ": " + id;
		if (HCTSharedConstants.currentIndividual==null){
			//A new individual being created
			if (mDbAdapter.confirmNewID(idType,id) && !inTemp(fullId)){
				HCTSharedConstants.tempIDs.add(fullId);
				HCTSharedConstants.currentIndividual=fullId;
				unique= true;
			}
		}
		else {
			//probably a swipe back
			HCTSharedConstants.tempIDs.remove(HCTSharedConstants.currentIndividual);
			HCTSharedConstants.tempIDs.add(fullId);
			HCTSharedConstants.currentIndividual=fullId;
			unique=true;
		}
		return unique;
	}
	
	private boolean confirmNewHousehold(String idType, String id){
		boolean unique=false;
		String fullId=idType + ": " + id;
		
		//if a saved form then current household is one saved in database
		if (HCTSharedConstants.savedFormName != null && HCTSharedConstants.householdId ==null)
			HCTSharedConstants.householdId=HCTSharedConstants.savedFormName;
		
		if (HCTSharedConstants.householdId==null){
			//A new household being created
			if (mDbAdapter.confirmNewID(idType,id) && !inTemp(fullId)){
				HCTSharedConstants.tempIDs.add(fullId);
				HCTSharedConstants.householdId=fullId;
				unique= true;
			}
		}
		else {
			//probably a swipe back
			HCTSharedConstants.tempIDs.remove(HCTSharedConstants.householdId);
			HCTSharedConstants.tempIDs.add(fullId);
			HCTSharedConstants.householdId=fullId;
			unique=true;
		}
		if (HCTSharedConstants.savedForm && HCTSharedConstants.savedFormName != null){
			if (!HCTSharedConstants.savedFormName.equalsIgnoreCase(fullId)){
				String table = HCTSharedConstants.savedFormName.substring(0, HCTSharedConstants.savedFormName.indexOf(":"));
				String idNum = HCTSharedConstants.savedFormName.substring(HCTSharedConstants.savedFormName.indexOf(":") + 2);
				if (mDbAdapter.deleteID(table, idNum))
					unique=true;
			}else
				unique=true;
		}
		return unique;
	}
	
    private  boolean confirmNewID(String idType, String id){
    	
    	//Finalizing a form: No need checking
    	if (HCTSharedConstants.finalizing) {
    		if (HCTSharedConstants.savedForm && idType.equalsIgnoreCase(HCTSharedConstants.HOUSEHOLD)
    				&& (HCTSharedConstants.householdId==null || HCTSharedConstants.householdId.trim()==""))
    			return confirmNewHousehold(idType, id);
    		else
    			return true;
    	}
    		
    	
    	//Initialize database connection
    	mDbAdapter=new HCTDbAdapter(HCTSharedConstants.dbCtx);
		mDbAdapter.open();
		
		//EITHER: confirm new household
		if (idType.equals(HCTSharedConstants.HOUSEHOLD))
			return confirmNewHousehold(idType, id);
		
		//OR: confirm new individual
		if (idType.equals(HCTSharedConstants.INDIVIDUAL))
			return confirmNewIndividual(idType, id);
    	
		mDbAdapter.close();
		return false;
    }
    
    private boolean inTemp(String id){
    	if (HCTSharedConstants.tempIDs.contains(id))
    		return true;
    	
    	return false;
    }
}