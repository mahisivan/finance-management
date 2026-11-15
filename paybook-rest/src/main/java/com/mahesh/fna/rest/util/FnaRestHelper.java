package com.mahesh.fna.rest.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mahesh.cf.customexeception.AppBaseExceptionLogger;
import com.mahesh.fna.app.vo.ReportSearchVO;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

public class FnaRestHelper {

	private FnaRestHelper() {

	}

	private static final Logger LOGGER = Logger.getLogger(FnaRestHelper.class.getName());

	/**
	 * Method to generate card transaction report
	 * @param searchVO
	 * @return
	 */
	public static ReportSearchVO generateCardTransactionReport(ReportSearchVO searchVO) {
		try {
			String driverClassName = searchVO.getAttributesMap().get("driverClassName").toString();
			String databaseUrl = searchVO.getAttributesMap().get("databaseUrl").toString();
			String dbUserName = searchVO.getAttributesMap().get("dbUserName").toString();
			String dbPassword = searchVO.getAttributesMap().get("dbPassword").toString();
			String bankCode = searchVO.getAttributesMap().get("bankCode").toString();
			
			Class.forName(driverClassName);
			try (Connection con = DriverManager.getConnection(databaseUrl, dbUserName, dbPassword)) {
				AppBaseExceptionLogger.printInfoMessage("Database Status", con == null ? "FAILED" : "SUCCESS");
				if (con != null) {
					Map<String, Object> hmap = new HashMap<String, Object>();
					hmap.put("IS_IGNORE_PAGINATION", false);
					hmap.put("bankCode", bankCode);
					hmap.put("fromDate", searchVO.getFromDate());
					hmap.put("toDate", searchVO.getToDate());
					hmap.put("imagesFolder", "images/Banners");
					searchVO = generateReport(con, hmap, "CardTransactionReport");
				}
			}
		} catch (Exception ex) {
			LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
		}
		return searchVO;
	}

	
	/**
	 * Method to generate report
	 * 
	 * @param con
	 * @param hmap
	 * @param reportName
	 */
	public static ReportSearchVO generateReport(Connection con, Map<String, Object> hmap, String templateName) {
		ReportSearchVO searchVO = new ReportSearchVO();
		try {
			String jrxmlFile = "src/main/resources/template/jrxml/".concat(templateName).concat(".jrxml");
			String jasperFile = "src/main/resources/template/jasper/".concat(templateName).concat(".jasper");
			String toDateFolder = hmap.get("toDate").toString().replace('/', '-').replace('\\', '-');

			AppBaseExceptionLogger.printInfoMessage("JRXML  : {0}", jrxmlFile);
			AppBaseExceptionLogger.printInfoMessage("JASPER : {0}", jasperFile);
			StringBuilder pdfFile = new StringBuilder();
			pdfFile.append("TemplateReport");
			pdfFile.append("/");
			pdfFile.append(toDateFolder);
			pdfFile.append("/");
			pdfFile.append(templateName);
			pdfFile.append(".pdf");
			hmap.put("outputFile", pdfFile.toString());
			File tempFile = new File(pdfFile.toString());
			File outputFolder = tempFile.getParentFile();
			outputFolder.mkdirs();
			AppBaseExceptionLogger.printInfoMessage("PDF : {0}", pdfFile.toString());

			JasperReport jasperReport;
			File jrxmlSource = new File(jrxmlFile);
			if (jrxmlSource.exists()) {
				JasperCompileManager.compileReportToFile(jrxmlFile, jasperFile);
				jasperReport = (JasperReport) JRLoader.loadObjectFromFile(jasperFile);
			} else {
				try (InputStream is = FnaRestHelper.class.getClassLoader().getResourceAsStream("jrxml/".concat(templateName).concat(".jrxml"))) {
					if (is == null) {
						throw new IllegalArgumentException("JRXML resource not found: jrxml/".concat(templateName).concat(".jrxml"));
					}
					JasperDesign jasperDesign = JRXmlLoader.load(is);
					jasperReport = JasperCompileManager.compileReport(jasperDesign);
				}
			}
			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, hmap, con);
			JasperExportManager.exportReportToPdfFile(jasperPrint, pdfFile.toString());			
			searchVO.getMessages().add(pdfFile.toString());
		} catch (JRException ex) {
			LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
			searchVO.getMessages().add(ex.getMessage());
			return null;
		} catch (IOException ex) {
			LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
			searchVO.getMessages().add(ex.getMessage());
		}
		return searchVO;
	}
}
