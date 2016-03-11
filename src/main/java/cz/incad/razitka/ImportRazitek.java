package cz.incad.razitka;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.aplikator.client.shared.data.FunctionParameters;
import org.aplikator.client.shared.data.FunctionResult;
import org.aplikator.client.shared.data.Operation;
import org.aplikator.client.shared.data.RecordContainerDTO;
import org.aplikator.client.shared.data.RecordDTO;
import org.aplikator.server.Context;
import org.aplikator.server.data.RecordUtils;
import org.aplikator.server.descriptor.WizardPage;
import org.aplikator.server.function.Executable;
import org.aplikator.server.persistence.tempstore.Tempstore;
import org.aplikator.server.persistence.tempstore.TempstoreFactory;
import org.aplikator.server.util.Configurator;

import com.typesafe.config.Config;

import cz.incad.razitka.server.Structure;

public class ImportRazitek extends Executable {

    Logger logger = Logger.getLogger(ImportRazitek.class.getName());



    @Override
    public FunctionResult execute(FunctionParameters parameters, Context context) {
        Config config = Configurator.get().getConfig();
        String importFolder = config.getString("razitka.importFolder");
        logger.info("STARTED IMPORT: "+importFolder);
        File importList = new File(importFolder);
        for (File file : importList.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (name != null && name.toLowerCase().endsWith(".xlsx")){
                    return true;
                }
                return false;
            }
        })) {
            logger.info("PROCESSING EXCEL FILE: "+file.getName());
            InputStream theFile = null;
            try {
                // open the zip file stream
                theFile = new FileInputStream(file);

                //Create Workbook instance holding reference to .xlsx file
                XSSFWorkbook workbook = new XSSFWorkbook(theFile);

                //Get first/desired sheet from the workbook
                XSSFSheet sheet = workbook.getSheetAt(0);



                Tempstore ts = TempstoreFactory.getTempstore();

                //Iterate through each rows one by one
                Iterator<Row> rowIterator = sheet.iterator();
                int rowNo = -2;
                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();
                    rowNo++;
                    //if (rowNo >200) break;
                    if (rowNo == -1) continue;

                    RecordContainerDTO rc = new RecordContainerDTO();
                    RecordDTO kniha = RecordUtils.newRecord(Structure.Exemplar);
                    String fileUI= null;

                    //For each row, iterate through all the columns
                    Iterator<Cell> cellIterator = row.cellIterator();

                    while (cellIterator.hasNext()) {
                        Cell cell = cellIterator.next();

                        //Check the cell type and format accordingly
                        switch (cell.getColumnIndex()) {
                            case 0:  //SIGNATURA
                                String signatura = cell.getStringCellValue().replaceAll(";\\s*","\n").trim();
                                Structure.Exemplar.signatura.setValue(kniha, signatura);
                                break;
                            case 1: //SYS
                                String sys = cell.getStringCellValue().replaceAll(";\\s*","\n").trim();
                                Structure.Exemplar.sys.setValue(kniha, sys);
                                break;
                            case 2: //UI
                                fileUI = cell.getStringCellValue();
                                break;
                            case 3: //NAPIS
                                String napis = cell.getStringCellValue();
                                Structure.Exemplar.napis.setValue(kniha, napis);
                                break;
                            case 5: //DRUH
                                String druh = cell.getStringCellValue().replaceAll(";","").trim();
                                Structure.Exemplar.druh.setValue(kniha, druh);
                                break;
                            case 6: //PRIJMENI
                                String prijmeni = cell.getStringCellValue().replaceAll(";","").trim();
                                Structure.Exemplar.prijmeni.setValue(kniha, prijmeni);
                                break;
                            case 7: //INSTIRUCE
                                String instituce = cell.getStringCellValue().replaceAll(";","").trim();
                                Structure.Exemplar.instituce.setValue(kniha, instituce);
                                break;
                            case 8: //OBECNE
                                String obecne = cell.getStringCellValue().replaceAll(";","").trim();
                                Structure.Exemplar.obecne.setValue(kniha, obecne);
                                break;
                            case 9: //MESTO
                                String mesto = cell.getStringCellValue().replaceAll(";","").trim();
                                Structure.Exemplar.mesto.setValue(kniha, mesto);
                                break;
                        }
                    }
                    System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!FILE:"+fileUI);
                    if (fileUI != null && !"".equals(fileUI)) {
                        final String fileprefix = fileUI;
                        File[] pictures = importList.listFiles(new FilenameFilter() {
                            public boolean accept(File dir, String name) {
                                if (name != null && name.toLowerCase().startsWith(fileprefix)){
                                    return true;
                                }
                                return false;
                            }
                        });
                        if (pictures.length==1) {
                            InputStream pictStream = new FileInputStream(pictures[0]);
                            String fileTempID = ts.store(pictures[0].getName(), pictStream, false);
                            kniha.setValue(Structure.Exemplar.obrazek.getId(), fileTempID);
                            pictStream.close();
                        }
                    }else{
                        continue;
                    }
                    rc.addRecord(Structure.Exemplar.view().getId(), kniha, kniha, Operation.CREATE);
                    rc = context.getAplikatorService().processRecords(rc);
                   // if (rowNo == allPictures.size()-1) break;

                }
                logger.info("KONEC:"+(rowNo+1));



            } catch (Throwable th) {
                logger.log(Level.SEVERE,"ERROR IMPORTING XLSX FILE: "+file.getName(),th);
                //return new FunctionResult("Chyba importu:"+th.getMessage(), false);
            } finally {
                // we must always close the zip file.
                try {
                    theFile.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                logger.info("FINISHED EXCEL FILE: "+file.getName());
            }
        }
        logger.info("FINISHED IMPORT: "+importFolder);
        return new FunctionResult("Importovano", true);

    }

    private String convertTypKnihy(String prefix){
        return "";
    }


    @Override
    public WizardPage getWizardPage(String currentPage, boolean forwardFlag, RecordDTO currentProcessingRecord, RecordDTO clientParameters, Context context) {
        return null;
    }

}
