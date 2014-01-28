/*
 * Copyright (C) 2013 Pavel Stastny
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.kramerius.rest.api.k5.client.pdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import net.sf.json.JSONObject;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.util.PDFMergerUtility;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.lowagie.text.DocumentException;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.document.DocumentService;
import cz.incad.kramerius.document.model.AbstractRenderedDocument;
import cz.incad.kramerius.pdf.FirstPagePDFService;
import cz.incad.kramerius.pdf.GeneratePDFService;
import cz.incad.kramerius.pdf.impl.ImageFetcher;
import cz.incad.kramerius.pdf.utils.PDFExlusiveGenerateSupport;
import cz.incad.kramerius.pdf.utils.pdf.FontMap;
import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

@Path("/v5.0/pdf")
public class PDFResource {

	public static Logger LOGGER = Logger.getLogger(PDFResource.class.getName());

	private boolean acquired;
	@Inject
	GeneratePDFService service;

	@Inject
	@Named("TEXT")
	FirstPagePDFService textFirstPage;

	@Inject
	@Named("IMAGE")
	FirstPagePDFService imageFirstPage;

	@Inject
	@Named("securedFedoraAccess")
	FedoraAccess fedoraAccess;

	@Inject
	KConfiguration configuration;

	@Inject
	SolrAccess solrAccess;

	@Inject
	DocumentService documentService;

	@Inject
	Provider<HttpServletRequest> requestProvider;

	@GET
	@Path("selection")
	@Produces({ "application/pdf", "application/json" })
	public Response selection(@QueryParam("pids") String pidsParam,
			@QueryParam("firstPageType") @DefaultValue("TEXT") String pageType,
			@QueryParam("format") String format) {
		try {
			acquired = PDFExlusiveGenerateSupport.PDF_SEMAPHORE.tryAcquire();
			if (acquired) {
				try {

					String imgServletUrl = ApplicationURL
							.applicationURL(this.requestProvider.get())
							+ "/img";
					if ((configuration.getApplicationURL() != null)
							&& (!configuration.getApplicationURL().equals(""))) {
						imgServletUrl = configuration.getApplicationURL()
								+ "img";
					}
					String i18nUrl = ApplicationURL
							.applicationURL(this.requestProvider.get())
							+ "/i18n";
					if ((configuration.getApplicationURL() != null)
							&& (!configuration.getApplicationURL().equals(""))) {
						i18nUrl = configuration.getApplicationURL() + "i18n";
					}
					FirstPage fp = pageType != null ? FirstPage
							.valueOf(pageType) : FirstPage.TEXT;
					String[] pids = pidsParam.split(",");
					// max number test
					checkNumber(pids);

					File f = null;
					if (fp == FirstPage.IMAGES) {
						f = selection(this.imageFirstPage, this.service,
								documentService, imgServletUrl, i18nUrl, pids,
								format);
					} else {
						f = selection(this.textFirstPage, this.service,
								documentService, imgServletUrl, i18nUrl, pids,
								format);
					}

					final File fileToDelete = f;

					final InputStream fis = new FileInputStream(f);
					StreamingOutput stream = new StreamingOutput() {
						public void write(OutputStream output)
								throws IOException, WebApplicationException {
							try {
								IOUtils.copyStreams(fis, output);
							} catch (Exception e) {
								throw new WebApplicationException(e);
							} finally {
								if (fileToDelete != null)
									fileToDelete.delete();
							}
						}
					};

					SimpleDateFormat sdate = new SimpleDateFormat(
							"yyyyMMdd_mmhhss");
					return Response
							.ok()
							.header("Content-disposition",
									"attachment; filename="
											+ sdate.format(new Date()) + ".pdf")
							.entity(stream).type("application/pdf").build();

				} catch (MalformedURLException e) {
					LOGGER.log(Level.SEVERE, e.getMessage(), e);
					throw new GenericApplicationException(e.getMessage());
				} catch (IOException e) {
					LOGGER.log(Level.SEVERE, e.getMessage(), e);
					throw new GenericApplicationException(e.getMessage());
				} catch (ProcessSubtreeException e) {
					LOGGER.log(Level.SEVERE, e.getMessage(), e);
					throw new GenericApplicationException(e.getMessage());
				} catch (COSVisitorException e) {
					LOGGER.log(Level.SEVERE, e.getMessage(), e);
					throw new GenericApplicationException(e.getMessage());
				} catch (DocumentException e) {
					LOGGER.log(Level.SEVERE, e.getMessage(), e);
					throw new GenericApplicationException(e.getMessage());
				}
			} else {
				throw new PDFResourceNotReadyException("not ready");
			}
		} finally {
			if (acquired)
				PDFExlusiveGenerateSupport.PDF_SEMAPHORE.release();
		}
	}

	private static void checkNumber(String[] pids) {
		String maxPage = KConfiguration.getInstance().getProperty(
				"generatePdfMaxRange");
		if (pids.length >= Integer.parseInt(maxPage)) {
			throw new PDFResourceBadRequestException("too much pages");
		}
	}

	@GET
	@Path("parent")
	@Produces({ "application/pdf", "application/json" })
	public Response parent(@QueryParam("pid") String pid,
			@QueryParam("number") String number,
			@QueryParam("firstPageType") @DefaultValue("TEXT") String pageType,
			@QueryParam("format") String format) {

		try {
			acquired = PDFExlusiveGenerateSupport.PDF_SEMAPHORE.tryAcquire();
			if (acquired) {
				try {
					String imgServletUrl = ApplicationURL
							.applicationURL(this.requestProvider.get())
							+ "/img";
					if ((configuration.getApplicationURL() != null)
							&& (!configuration.getApplicationURL().equals(""))) {
						imgServletUrl = configuration.getApplicationURL()
								+ "img";
					}
					String i18nUrl = ApplicationURL
							.applicationURL(this.requestProvider.get())
							+ "/i18n";
					if ((configuration.getApplicationURL() != null)
							&& (!configuration.getApplicationURL().equals(""))) {
						i18nUrl = configuration.getApplicationURL() + "i18n";
					}
					FirstPage fp = pageType != null ? FirstPage
							.valueOf(pageType) : FirstPage.TEXT;
					if (number == null || number.trim().equals(""))
						number = "" + (Integer.parseInt(number) - 1);
					checkNumber(number);

					File f = null;
					if (fp == FirstPage.IMAGES) {
						f = parent(pid, number, this.imageFirstPage,
								this.service, solrAccess, documentService,
								imgServletUrl, i18nUrl, format);
					} else {
						f = parent(pid, number, this.textFirstPage,
								this.service, solrAccess, documentService,
								imgServletUrl, i18nUrl, format);
					}

					final File fileToDelete = f;

					final InputStream fis = new FileInputStream(f);
					StreamingOutput stream = new StreamingOutput() {
						public void write(OutputStream output)
								throws IOException, WebApplicationException {
							try {
								IOUtils.copyStreams(fis, output);
							} catch (Exception e) {
								throw new WebApplicationException(e);
							} finally {
								if (fileToDelete != null)
									fileToDelete.delete();
							}
						}
					};

					SimpleDateFormat sdate = new SimpleDateFormat(
							"yyyyMMdd_mmhhss");
					return Response
							.ok()
							.header("Content-disposition",
									"attachment; filename="
											+ sdate.format(new Date()) + ".pdf")
							.entity(stream).type("application/pdf").build();
				} catch (NumberFormatException e) {
					LOGGER.log(Level.SEVERE, e.getMessage(), e);
					throw new GenericApplicationException(e.getMessage());
				} catch (COSVisitorException e) {
					LOGGER.log(Level.SEVERE, e.getMessage(), e);
					throw new GenericApplicationException(e.getMessage());
				} catch (FileNotFoundException e) {
					LOGGER.log(Level.SEVERE, e.getMessage(), e);
					throw new GenericApplicationException(e.getMessage());
				} catch (DocumentException e) {
					LOGGER.log(Level.SEVERE, e.getMessage(), e);
					throw new GenericApplicationException(e.getMessage());
				} catch (IOException e) {
					LOGGER.log(Level.SEVERE, e.getMessage(), e);
					throw new GenericApplicationException(e.getMessage());
				} catch (ProcessSubtreeException e) {
					LOGGER.log(Level.SEVERE, e.getMessage(), e);
					throw new GenericApplicationException(e.getMessage());
				}
			} else {
				throw new PDFResourceNotReadyException("not ready");

			}

		} finally {
			if (acquired)
				PDFExlusiveGenerateSupport.PDF_SEMAPHORE.release();
		}

	}

	@GET
	@Path("conf")
	@Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
	public Response info() {
		JSONObject jsonObject = new JSONObject();
		String maxPage = KConfiguration.getInstance().getProperty(
				"generatePdfMaxRange");
		jsonObject.put("maxpage", maxPage);
		return Response.ok().entity(jsonObject.toString()).build();
	}

	public static File parent(String pid, String number,
			FirstPagePDFService firstPagePDFService,
			GeneratePDFService pdfService, SolrAccess solrAccess,
			DocumentService documentService, String imgServletUrl,
			String i18nUrl, String srect) throws DocumentException,
			IOException, COSVisitorException, NumberFormatException,
			ProcessSubtreeException {

		FileOutputStream generatedPDFFos = null;

		FontMap fmap = new FontMap(pdfService.fontsFolder());

		checkNumber(number);

		File tmpFile = File.createTempFile("body", "pdf");

		FileOutputStream bodyTmpFos = new FileOutputStream(tmpFile);
		File fpage = File.createTempFile("head", "pdf");

		FileOutputStream fpageFos = new FileOutputStream(fpage);

		ObjectPidsPath[] paths = solrAccess.getPath(pid);
		final ObjectPidsPath path = selectOnePath(pid, paths);

		int[] irects = srect(srect);

		AbstractRenderedDocument rdoc = documentService.buildDocumentAsFlat(
				path, pid, Integer.parseInt(number), irects);
		if (rdoc.getPages().isEmpty()) {
			rdoc = documentService.buildDocumentAsFlat(path, path.getLeaf(),
					Integer.parseInt(number), irects);
		}

		firstPagePDFService.generateFirstPageForParent(rdoc, fpageFos, path,
				imgServletUrl, i18nUrl, fmap);

		pdfService.generateCustomPDF(rdoc, bodyTmpFos, fmap, imgServletUrl,
				i18nUrl, ImageFetcher.WEB);

		bodyTmpFos.close();
		fpageFos.close();

		File generatedPDF = File.createTempFile("rendered", "pdf");
		generatedPDFFos = new FileOutputStream(generatedPDF);

		mergeToOutput(generatedPDFFos, tmpFile, fpage);

		return generatedPDF;

	}

	private static void checkNumber(String number) {
		String maxPage = KConfiguration.getInstance().getProperty(
				"generatePdfMaxRange");
		if (Integer.parseInt(number) >= Integer.parseInt(maxPage)) {
			throw new PDFResourceBadRequestException("too much pages");
		}
	}

	static File selection(FirstPagePDFService firstPagePDFService,
			GeneratePDFService pdfService, DocumentService documentService,
			String imgServletUrl, String i18nUrl, String[] pids, String srect)
			throws IOException, FileNotFoundException, DocumentException,
			ProcessSubtreeException, COSVisitorException {

		List<File> filesToDelete = new ArrayList<File>();
		FileOutputStream generatedPDFFos = null;

		File tmpFile = File.createTempFile("body", "pdf");
		filesToDelete.add(tmpFile);
		FileOutputStream bodyTmpFos = new FileOutputStream(tmpFile);
		File fpage = File.createTempFile("head", "pdf");
		filesToDelete.add(fpage);
		FileOutputStream fpageFos = new FileOutputStream(fpage);

		int[] irects = srect(srect);

		FontMap fMap = new FontMap(pdfService.fontsFolder());

		AbstractRenderedDocument rdoc = documentService
				.buildDocumentFromSelection(pids, irects);

		firstPagePDFService.generateFirstPageForSelection(rdoc, fpageFos, pids,
				imgServletUrl, i18nUrl, fMap);

		pdfService.generateCustomPDF(rdoc, bodyTmpFos, fMap, imgServletUrl,
				i18nUrl, ImageFetcher.WEB);

		bodyTmpFos.close();
		fpageFos.close();

		File generatedPDF = File.createTempFile("rendered", "pdf");
		generatedPDFFos = new FileOutputStream(generatedPDF);

		mergeToOutput(generatedPDFFos, tmpFile, fpage);

		return generatedPDF;
	}

	static void mergeToOutput(OutputStream fos, File bodyFile,
			File firstPageFile) throws IOException, COSVisitorException {
		PDFMergerUtility utility = new PDFMergerUtility();
		utility.addSource(firstPageFile);
		utility.addSource(bodyFile);
		utility.setDestinationStream(fos);
		utility.mergeDocuments();
	}

	static int[] srect(String srect) {
		int[] rect = null;
		if (srect != null) {
			String[] arr = srect.split(",");
			if (arr.length == 2) {
				rect = new int[2];
				rect[0] = Integer.parseInt(arr[0]);
				rect[1] = Integer.parseInt(arr[1]);
			}
		}
		return rect;
	}

	static ObjectPidsPath selectOnePath(String requestedPid,
			ObjectPidsPath[] paths) {
		ObjectPidsPath path;
		if (paths.length > 0) {
			path = paths[0];
		} else {
			path = new ObjectPidsPath(requestedPid);
		}
		return path;
	}

	public enum FirstPage {
		IMAGES, TEXT;
	}
}
