/* eXist Native XML Database
 * Copyright (C) 2000-03,  Wolfgang M. Meier (wolfgang@exist-db.org)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 * $Id$
 */
package org.exist.util.serializer;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;

import org.exist.dom.QName;
import org.exist.util.XMLString;
import org.exist.util.serializer.encodings.CharacterSet;

/**
 * Write XML to a writer. This class defines methods similar to SAX.
 * It deals with opening and closing tags, writing attributes and so on.
 * 
 * @author wolf
 */
public class XMLWriter {

	protected final static Properties defaultProperties = new Properties();
	static {
		defaultProperties.setProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
	}
	
	protected Writer writer = null;
	protected CharacterSet charSet = null;
	protected boolean tagIsOpen = false;
	protected boolean tagIsEmpty = true;
	protected boolean declarationWritten = false;

	protected Properties outputProperties;

	private char[] charref = new char[10];

	private static boolean[] textSpecialChars;
	private static boolean[] attrSpecialChars;
	
	static {
		textSpecialChars = new boolean[128];
		Arrays.fill(textSpecialChars, false);
		textSpecialChars['<'] = true;
		textSpecialChars['>'] = true;
		textSpecialChars['\r'] = true;
		textSpecialChars['&'] = true;
		
		attrSpecialChars = new boolean[128];
		Arrays.fill(attrSpecialChars, false);
		attrSpecialChars['<'] = true;
		attrSpecialChars['>'] = true;
		attrSpecialChars['\r'] = true;
		attrSpecialChars['\n'] = true;
		attrSpecialChars['\t'] = true;
		attrSpecialChars['&'] = true;
		attrSpecialChars['"'] = true;
	}

	public XMLWriter() {
	}
	
	public XMLWriter(Writer writer) {
		super();
		this.writer = writer;
	}

	/**
	 * Set the output properties.
	 * 
	 * @param outputProperties
	 */
	public void setOutputProperties(Properties properties) {
		if(properties == null)
			outputProperties = defaultProperties;
		else
			outputProperties = properties;
		String encoding =
			outputProperties.getProperty(OutputKeys.ENCODING, "UTF-8");
		charSet = CharacterSet.getCharacterSet(encoding);
	}

	/**
	 * Set a new writer. Calling this method will reset the state
	 * of the object.
	 * 
	 * @param writer
	 */
	public void setWriter(Writer writer) {
		this.writer = writer;
		tagIsOpen = false;
		tagIsEmpty = true;
		declarationWritten = false;
	}
	
	public void startDocument() throws TransformerException {
		tagIsOpen = false;
		tagIsEmpty = true;
		declarationWritten = false;
	}

	public void endDocument() throws TransformerException {
	}

	public void startElement(String qname) throws TransformerException {
		if (!declarationWritten)
			writeDeclaration();
		try {
			if (tagIsOpen)
				closeStartTag(false);
			writer.write('<');
			writer.write(qname);
			tagIsOpen = true;
		} catch (IOException e) {
			throw new TransformerException(e.getMessage(), e);
		}
	}

	public void startElement(QName qname) throws TransformerException {
		if (!declarationWritten)
			writeDeclaration();
		try {
			if (tagIsOpen)
				closeStartTag(false);
			writer.write('<');
			if(qname.getPrefix() != null && qname.getPrefix().length() > 0) {
				writer.write(qname.getPrefix());
				writer.write(':');
			}
			writer.write(qname.getLocalName());
			tagIsOpen = true;
		} catch (IOException e) {
			throw new TransformerException(e.getMessage(), e);
		}
	}
	
	public void endElement(String qname) throws TransformerException {
		try {
			if (tagIsOpen)
				closeStartTag(true);
			else {
				writer.write("</");
				writer.write(qname);
				writer.write('>');
			}
		} catch (IOException e) {
			throw new TransformerException(e.getMessage(), e);
		}
	}

	public void endElement(QName qname) throws TransformerException {
		try {
			if (tagIsOpen)
				closeStartTag(true);
			else {
				writer.write("</");
				if(qname.getPrefix() != null && qname.getPrefix().length() > 0) {
					writer.write(qname.getPrefix());
					writer.write(':');
				}
				writer.write(qname.getLocalName());
				writer.write('>');
			}
		} catch (IOException e) {
			throw new TransformerException(e.getMessage(), e);
		}
	}
	
	public void namespace(String prefix, String nsURI)
		throws TransformerException {
		if ((nsURI == null || nsURI.length() == 0)
			&& (prefix == null || prefix.length() == 0))
			return;
		try {
			if (!tagIsOpen)
				throw new TransformerException("Found a namespace declaration outside an element");
			writer.write(' ');
			writer.write("xmlns");
			if (prefix != null && prefix.length() > 0) {
				writer.write(':');
				writer.write(prefix);
			}
			writer.write("=\"");
			writeChars(nsURI, true);
			writer.write('"');
		} catch (IOException e) {
			throw new TransformerException(e.getMessage(), e);
		}
	}

	public void attribute(String qname, String value)
		throws TransformerException {
		try {
			if (!tagIsOpen) {
				characters(value);
				return;
//				throw new TransformerException("Found an attribute outside an element");
			}
			writer.write(' ');
			writer.write(qname);
			writer.write("=\"");
			writeChars(value, true);
			writer.write('"');
		} catch (IOException e) {
			throw new TransformerException(e.getMessage(), e);
		}
	}

	public void attribute(QName qname, String value) throws TransformerException {
		try {
			if (!tagIsOpen) {
				characters(value);
				return;
//				throw new TransformerException("Found an attribute outside an element");
			}	
			writer.write(' ');
			if(qname.getPrefix() != null && qname.getPrefix().length() > 0) {
				writer.write(qname.getPrefix());
				writer.write(':');
			}
			writer.write(qname.getLocalName());
			writer.write("=\"");
			writeChars(value, true);
			writer.write('"');
		} catch (IOException e) {
			throw new TransformerException(e.getMessage(), e);
		}
	}
	
	public void characters(CharSequence chars) throws TransformerException {
		if (!declarationWritten)
			writeDeclaration();
		try {
			if (tagIsOpen)
				closeStartTag(false);
			writeChars(chars, false);
		} catch (IOException e) {
			throw new TransformerException(e.getMessage(), e);
		}
	}

	public void characters(char[] ch, int start, int len)
		throws TransformerException {
		if (!declarationWritten)
			writeDeclaration();
		XMLString s = new XMLString(ch, start, len);
		characters(s);
		s.release();
	}

	public void processingInstruction(String target, String data)
		throws TransformerException {
		if (!declarationWritten)
			writeDeclaration();
		try {
			if (tagIsOpen)
				closeStartTag(false);
			writer.write("<?");
			writer.write(target);
			if (data != null && data.length() > 0) {
				writer.write(' ');
				writeChars(data, false);
			}
			writer.write("?>");
		} catch (IOException e) {
			throw new TransformerException(e.getMessage(), e);
		}
	}

	public void comment(CharSequence data) throws TransformerException {
		if (!declarationWritten)
			writeDeclaration();
		try {
			if (tagIsOpen)
				closeStartTag(false);
			writer.write("<!--");
			writeChars(data, false);
			writer.write("-->");
		} catch (IOException e) {
			throw new TransformerException(e.getMessage(), e);
		}
	}

    public void cdataSection(char[] ch, int start, int len) throws TransformerException {
        if (tagIsOpen)
            closeStartTag(false);
        try {
            writer.write("<![CDATA[");
            writer.write(ch, start, len);
            writer.write("]]>");
        } catch (IOException e) {
            throw new TransformerException(e.getMessage(), e);
        }
    }
    
	protected void closeStartTag(boolean isEmpty) throws TransformerException {
		try {
			if (tagIsOpen) {
				if (isEmpty)
					writer.write("/>");
				else
					writer.write('>');
				tagIsOpen = false;
			}
		} catch (IOException e) {
			throw new TransformerException(e.getMessage(), e);
		}
	}

	protected void writeDeclaration() throws TransformerException {
		if (declarationWritten)
			return;
		if(outputProperties == null)
			outputProperties = defaultProperties;
		declarationWritten = true;
		String omitXmlDecl =
			outputProperties.getProperty(
				OutputKeys.OMIT_XML_DECLARATION,
				"yes");
		String version =
			outputProperties.getProperty(OutputKeys.VERSION, "1.0");
		String standalone = outputProperties.getProperty(OutputKeys.STANDALONE);
		String encoding =
			outputProperties.getProperty(OutputKeys.ENCODING, "UTF-8");
		if (omitXmlDecl.equals("no")) {
			try {
				writer.write("<?xml version=\"");
				writer.write(version);
				writer.write("\" encoding=\"");
				writer.write(encoding);
				writer.write('"');
				if (standalone != null) {
					writer.write(" standalone=\"");
					writer.write(standalone);
					writer.write('"');
				}
				writer.write("?>\n");
			} catch (IOException e) {
				throw new TransformerException(e.getMessage(), e);
			}
		}
	}

	private final void writeChars(CharSequence s, boolean inAttribute) throws IOException {
	    boolean[] specialChars = inAttribute ? attrSpecialChars : textSpecialChars;
		char ch = 0;
		final int len = s.length();
		int pos = 0, i;
		while(pos < len) {
		    i = pos;
		    while(i < len) {
		        ch = s.charAt(i);
		        if(ch < 128) {
		            if(specialChars[ch])
		                break;
		            else
		                i++;
		        } else if(!charSet.inCharacterSet(ch))
		            break;
		        else
		            i++;
		    }
		    writeCharSeq(s, pos, i);
//	        writer.write(s.subSequence(pos, i).toString());
		    if(i >= len)
		        return;
		    switch (ch) {
				case '<' :
					writer.write("&lt;");
					break;
				case '>' :
					writer.write("&gt;");
					break;
				case '&' :
					writer.write("&amp;");
					break;
				case '\r' :
					writer.write("&#xD;");
					break;
				case '\n' :
					writer.write("&#xA;");
					break;
				case '\t' :
					writer.write("&#x9;");
					break;
				case '"' :
					writer.write("&#34;");
					break;
				default:
				    writeCharacterReference(ch);
		    }
		    pos = ++i;
		}
	}

	private void writeCharSeq(CharSequence ch, int start, int end) throws IOException {
		for(int i = start; i < end; i++) {
			writer.write(ch.charAt(i));
		}
	}
	
	protected void writeCharacterReference(char charval) throws IOException {
		int o = 0;
		charref[o++] = '&';
		charref[o++] = '#';
		charref[o++] = 'x';
		String code = Integer.toHexString(charval);
		int len = code.length();
		for (int k = 0; k < len; k++) {
			charref[o++] = code.charAt(k);
		}
		charref[o++] = ';';
		writer.write(charref, 0, o);
	}
}
