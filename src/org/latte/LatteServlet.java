package org.latte;

import org.latte.scripting.hostobjects.RequestProxy;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.latte.scripting.PrimitiveWrapFactory;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class LatteServlet extends HttpServlet {
	private static final Logger LOG = Logger.getLogger(LatteServlet.class);
	final private Scriptable parent;
	final private Callable fn;
	
	public LatteServlet(Scriptable parent, Callable fn) {
		this.parent = parent;
		this.fn = fn;
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 5876743891237403945L;

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {			
		try {
			Context cx = ContextFactory.getGlobal().enterContext();

			Scriptable session;
			if((session = (Scriptable)request.getSession().getAttribute("latte.session")) == null) {
				session = new ScriptableObject() {
					@Override
					public String getClassName() { return "Session"; }
				};
	    			session.put("id", session, request.getSession().getId());
				request.getSession().setAttribute("latte.session", session);
			}
			
			Scriptable scope = cx.newObject(parent);
			scope.setParentScope(parent);
			cx.setWrapFactory(new PrimitiveWrapFactory());
			fn.call(cx, scope, scope, new Object[] {
					new RequestProxy(request),
					response,
					session
			});
		} catch(Exception e) {
			LOG.fatal("", e);
			response.sendError(500);
		} finally {
			Context.exit();
		}
	}
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
}