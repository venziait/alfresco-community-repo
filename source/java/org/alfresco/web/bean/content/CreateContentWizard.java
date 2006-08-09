/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.web.bean.content;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.alfresco.config.Config;
import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigService;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;
import org.alfresco.web.templating.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.ContentWriter;
import java.io.OutputStreamWriter;

/**
 * Bean implementation for the "Create Content Wizard" dialog
 * 
 * @author gavinc
 */
public class CreateContentWizard extends BaseContentWizard
{
    protected String content = null;
    protected String templateType;
    protected List<SelectItem> createMimeTypes;
    
    private static final Log LOGGER = 
	LogFactory.getLog(CreateContentWizard.class);

    public static final org.alfresco.service.namespace.QName TT_QNAME = 
	org.alfresco.service.namespace.QName.createQName(org.alfresco.service.namespace.NamespaceService.CONTENT_MODEL_1_0_URI, "tt");

   // ------------------------------------------------------------------------------
   // Wizard implementation
   
   @Override
   protected String finishImpl(FacesContext context, String outcome)
         throws Exception
   {
       LOGGER.debug("saving file content to " + this.fileName);
       saveContent(null, this.content);
       if (this.templateType != null)
       {
	   LOGGER.debug("generating template output for " + this.templateType);
	   this.nodeService.setProperty(this.createdNode, TT_QNAME, this.templateType);
	   TemplatingService ts = TemplatingService.getInstance();
	   TemplateType tt = ts.getTemplateType(this.templateType);
	   if (tt.getOutputMethods().size() != 0)
	   {
	       try {
		   // get the node ref of the node that will contain the content
		   NodeRef containerNodeRef = this.getContainerNodeRef();
		   final String fileName = this.fileName + "-generated.html";
		   FileInfo fileInfo = 
		       this.fileFolderService.create(containerNodeRef,
						     fileName,
						     ContentModel.TYPE_CONTENT);
		   NodeRef fileNodeRef = fileInfo.getNodeRef();
      
		   if (LOGGER.isDebugEnabled())
		       LOGGER.debug("Created file node for file: " + 
				    fileName);
	
		   // get a writer for the content and put the file
		   ContentWriter writer = contentService.getWriter(fileNodeRef, 
								   ContentModel.PROP_CONTENT, true);
		   // set the mimetype and encoding
		   writer.setMimetype("text/html");
		   writer.setEncoding("UTF-8");
		   TemplateOutputMethod tom = tt.getOutputMethods().get(0);
		   OutputStreamWriter out = 
		       new OutputStreamWriter(writer.getContentOutputStream());
		   tom.generate(ts.parseXML(this.content), tt, out);
		   out.close();
		   this.nodeService.setProperty(fileNodeRef, TT_QNAME, this.templateType);

		   LOGGER.debug("generated " + fileName + " using " + tom);
	       }
	       catch (Exception e)
               {
		   e.printStackTrace();
		   throw e;
	       }
	   }
       }

       // return the default outcome
       return outcome;
   }
   
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      this.content = null;
      this.inlineEdit = true;
      this.templateType = null;
      this.mimeType = MimetypeMap.MIMETYPE_HTML;
   }
   
   @Override
   public boolean getNextButtonDisabled()
   {
      // TODO: Allow the next button state to be configured so that
      //       wizard implementations don't have to worry about 
      //       checking step numbers
      
      boolean disabled = false;
      int step = Application.getWizardManager().getCurrentStep();
      switch(step)
      {
         case 1:
         {
            disabled = (this.fileName == null || this.fileName.length() == 0);
            break;
         }
      }
      
      return disabled;
   }
   
   @Override
   protected String doPostCommitProcessing(FacesContext context, String outcome)
   {
      // as we were successful, go to the set properties dialog if asked
      // to otherwise just return
      if (this.showOtherProperties)
      {
         // we are going to immediately edit the properties so we need
         // to setup the BrowseBean context appropriately
         this.browseBean.setDocument(new Node(this.createdNode));
      
         return getDefaultFinishOutcome() + AlfrescoNavigationHandler.OUTCOME_SEPARATOR + 
                "dialog:setContentProperties";
      }
      else
      {
         return outcome;
      }
   }
   
   // ------------------------------------------------------------------------------
   // Bean Getters and Setters
   
   /**
    * @return Returns the content from the edited form.
    */
   public String getContent()
   {
      return this.content;
   }
   
   /**
    * @param content The content to edit (should be clear initially)
    */
   public void setContent(String content)
   {
      this.content = content;
   }

    public List<SelectItem> getCreateTemplateTypes()
    {
	List<TemplateType> ttl = TemplatingService.getInstance().getTemplateTypes();
	List<SelectItem> sil = new ArrayList<SelectItem>(ttl.size());
	Iterator it = ttl.iterator();
	while (it.hasNext())
	{
	    TemplateType tt = (TemplateType)it.next();
	    sil.add(new SelectItem(tt.getName(), tt.getName()));
	}
	return sil;
    }
   
   /**
    * @return Returns a list of mime types to allow the user to select from
    */
   public List<SelectItem> getCreateMimeTypes()
   {
      if (this.createMimeTypes == null)
      {
         FacesContext context = FacesContext.getCurrentInstance();
         
         // add the well known object type to start with
         this.createMimeTypes = new ArrayList<SelectItem>(5);
         
         // add the configured create mime types to the list
         ConfigService svc = Application.getConfigService(context);
         Config wizardCfg = svc.getConfig("Content Wizards");
         if (wizardCfg != null)
         {
            ConfigElement typesCfg = wizardCfg.getConfigElement("create-mime-types");
            if (typesCfg != null)
            {
               for (ConfigElement child : typesCfg.getChildren())
               {
                  String currentMimeType = child.getAttribute("name");
                  if (currentMimeType != null)
                  {
                     String label = getSummaryMimeType(currentMimeType);
                     this.createMimeTypes.add(new SelectItem(currentMimeType, label));
                  }
               }
               
               // make sure the list is sorted by the label
               QuickSort sorter = new QuickSort(this.objectTypes, "label", true, IDataContainer.SORT_CASEINSENSITIVE);
               sorter.sort();
            }
            else
            {
               LOGGER.warn("Could not find 'create-mime-types' configuration element");
            }
         }
         else
         {
            LOGGER.warn("Could not find 'Content Wizards' configuration section");
         }
         
      }
      
      return this.createMimeTypes;
   }

   public String getTemplateType()
   {
      return this.templateType;
   }

   /**
    * @param templateType Sets the currently selected template type
    */
   public void setTemplateType(String templateType)
   {
      this.templateType = templateType;
   }
   
   /**
    * @return Returns the summary data for the wizard.
    */
   public String getSummary()
   {
      ResourceBundle bundle = Application.getBundle(FacesContext.getCurrentInstance());
      
      // TODO: show first few lines of content here?
      return buildSummary(
            new String[] {bundle.getString("file_name"), 
                          bundle.getString("type"), 
                          bundle.getString("content_type")},
            new String[] {this.fileName, getSummaryObjectType(), 
                          getSummaryMimeType(this.mimeType)});
   }
   
   // ------------------------------------------------------------------------------
   // Action event handlers
      
   /**
    * Create content type value changed by the user
    */
   public void createContentChanged(ValueChangeEvent event)
   {
      // clear the content as HTML is not compatible with the plain text box etc.
      this.content = null;
   }
   
   // ------------------------------------------------------------------------------
   // Service Injection

   
   // ------------------------------------------------------------------------------
   // Helper methods
   
}
