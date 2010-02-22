package cz.incad.Kramerius.backend.pdf;

import java.io.IOException;
import java.text.SimpleDateFormat;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.utils.IKeys;

public class GeneratePDFServlet extends GuiceServlet {

	@Inject
	GeneratePDFService service;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String uuid = req.getParameter(IKeys.UUID_PARAMETER);
		if (uuid != null) {
			resp.setContentType("application/pdf");
	        resp.setHeader("Content-disposition","inline; filename=Some.pdf");
			service.generatePDF(uuid, resp.getOutputStream());
		}
	}
	
}
