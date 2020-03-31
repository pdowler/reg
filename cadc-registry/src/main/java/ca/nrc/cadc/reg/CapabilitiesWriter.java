/*
************************************************************************
*******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
**************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
*
*  (c) 2020.                            (c) 2020.
*  Government of Canada                 Gouvernement du Canada
*  National Research Council            Conseil national de recherches
*  Ottawa, Canada, K1A 0R6              Ottawa, Canada, K1A 0R6
*  All rights reserved                  Tous droits réservés
*
*  NRC disclaims any warranties,        Le CNRC dénie toute garantie
*  expressed, implied, or               énoncée, implicite ou légale,
*  statutory, of any kind with          de quelque nature que ce
*  respect to the software,             soit, concernant le logiciel,
*  including without limitation         y compris sans restriction
*  any warranty of merchantability      toute garantie de valeur
*  or fitness for a particular          marchande ou de pertinence
*  purpose. NRC shall not be            pour un usage particulier.
*  liable in any event for any          Le CNRC ne pourra en aucun cas
*  damages, whether direct or           être tenu responsable de tout
*  indirect, special or general,        dommage, direct ou indirect,
*  consequential or incidental,         particulier ou général,
*  arising from the use of the          accessoire ou fortuit, résultant
*  software.  Neither the name          de l'utilisation du logiciel. Ni
*  of the National Research             le nom du Conseil National de
*  Council of Canada nor the            Recherches du Canada ni les noms
*  names of its contributors may        de ses  participants ne peuvent
*  be used to endorse or promote        être utilisés pour approuver ou
*  products derived from this           promouvoir les produits dérivés
*  software without specific prior      de ce logiciel sans autorisation
*  written permission.                  préalable et particulière
*                                       par écrit.
*
*  This file is part of the             Ce fichier fait partie du projet
*  OpenCADC project.                    OpenCADC.
*
*  OpenCADC is free software:           OpenCADC est un logiciel libre ;
*  you can redistribute it and/or       vous pouvez le redistribuer ou le
*  modify it under the terms of         modifier suivant les termes de
*  the GNU Affero General Public        la “GNU Affero General Public
*  License as published by the          License” telle que publiée
*  Free Software Foundation,            par la Free Software Foundation
*  either version 3 of the              : soit la version 3 de cette
*  License, or (at your option)         licence, soit (à votre gré)
*  any later version.                   toute version ultérieure.
*
*  OpenCADC is distributed in the       OpenCADC est distribué
*  hope that it will be useful,         dans l’espoir qu’il vous
*  but WITHOUT ANY WARRANTY;            sera utile, mais SANS AUCUNE
*  without even the implied             GARANTIE : sans même la garantie
*  warranty of MERCHANTABILITY          implicite de COMMERCIALISABILITÉ
*  or FITNESS FOR A PARTICULAR          ni d’ADÉQUATION À UN OBJECTIF
*  PURPOSE.  See the GNU Affero         PARTICULIER. Consultez la Licence
*  General Public License for           Générale Publique GNU Affero
*  more details.                        pour plus de détails.
*
*  You should have received             Vous devriez avoir reçu une
*  a copy of the GNU Affero             copie de la Licence Générale
*  General Public License along         Publique GNU Affero avec
*  with OpenCADC.  If not, see          OpenCADC ; si ce n’est
*  <http://www.gnu.org/licenses/>.      pas le cas, consultez :
*                                       <http://www.gnu.org/licenses/>.
*
************************************************************************
*/

package ca.nrc.cadc.reg;

import ca.nrc.cadc.xml.W3CConstants;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 * Write a Capabilities object in XML format.
 * 
 * @author pdowler
 */
public class CapabilitiesWriter {
    private static final Logger log = Logger.getLogger(CapabilitiesWriter.class);

    Namespace vosi = Namespace.getNamespace("vosi", "http://www.ivoa.net/xml/VOSICapabilities/v1.0");
    Namespace vs = Namespace.getNamespace("vs", "http://www.ivoa.net/xml/VODataService/v1.1");
        
    public CapabilitiesWriter() { 
    }
    
    public void write(Capabilities caps, OutputStream out) throws IOException {
        write(caps, new OutputStreamWriter(out, "UTF-8"));
    }
    
    public void write(Capabilities caps, Writer out) throws IOException {
        Element root = getRootElement(caps);
        write(root, out);
    }
    
    protected void write(Element root, Writer writer) throws IOException {
        XMLOutputter outputter = new XMLOutputter();
        outputter.setFormat(Format.getPrettyFormat());
        Document document = new Document(root);
        outputter.output(document, writer);
    }
    
    private Element getRootElement(Capabilities caps) {
        Element root = new Element("capabilities", vosi);
        root.addNamespaceDeclaration(vs);
        root.addNamespaceDeclaration(W3CConstants.XSI_NS);
        
        for (Capability c : caps.getCapabilities()) {
            Element ce = getCapabilityElement(c);
            root.addContent(ce);
        }
        
        return root;
    }
    
    private Element getCapabilityElement(Capability c) {
        Element ret = new Element("capability", Namespace.NO_NAMESPACE);
        ret.setAttribute("standardID", c.getStandardID().toASCIIString());
        boolean ext = false;
        if (c.getExtensionNamespace() != null && c.getExtensionType() != null) {
            ret.addNamespaceDeclaration(c.getExtensionNamespace());
            ret.setAttribute(c.getExtensionType());
            ext = true;
        }
        
        // interfaces
        for (Interface i : c.getInterfaces()) {
            Element ie = getInterfaceElement(i, ret.getNamespacesInScope());
            ret.addContent(ie);
        }
        
        // extensions
        if (ext) {
            ret.addContent(c.getExtensionMetadata());
        }
        
        return ret;
    }
    
    private Element getInterfaceElement(Interface i, List<Namespace> nsInScope) {
        Element ret = new Element("interface", Namespace.NO_NAMESPACE);
        URI type = i.getType();
        String stype = "vs:" + type.getFragment();
        ret.setAttribute("type", stype, W3CConstants.XSI_NS);
        
        if (i.role != null) {
            ret.setAttribute("role", i.role, Namespace.NO_NAMESPACE);
        }
        if (i.version != null) {
            ret.setAttribute("version", i.version, Namespace.NO_NAMESPACE);
        }
        
        // access URLs
        Element aue = getAccessURLElement(i.getAccessURL());
        ret.addContent(aue);
        
        // security methods
        for (URI uri : i.getSecurityMethods()) {
            Element sme = getSecurityMethodElement(uri);
            ret.addContent(sme);
        }
        
        return  ret;
    }
    
    private Element getAccessURLElement(AccessURL a) {
        Element ret = new Element("accessURL", Namespace.NO_NAMESPACE);
        if (a.use != null) {
            ret.setAttribute("use", a.use);
        }
        ret.setText(a.getURL().toExternalForm());
        return ret;
    }
    
    private Element getSecurityMethodElement(URI s) {
        Element ret = new Element("securityMethod", Namespace.NO_NAMESPACE);
        ret.setAttribute("standardID", s.toASCIIString());
        return ret;
    }
}
