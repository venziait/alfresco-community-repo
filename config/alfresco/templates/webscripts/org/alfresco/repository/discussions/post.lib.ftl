<#-- Renders a person object. -->
<#macro renderPerson person fieldName>
   "${fieldName}" : {
      <#if person.assocs["cm:avatar"]??>
      "avatarRef" : "${person.assocs["cm:avatar"][0].nodeRef?string}",
      </#if>
      "username" : "${person.properties["cm:userName"]}",
      "firstName" : "${person.properties["cm:firstName"]?html}",
      "lastName" : "${person.properties["cm:lastName"]?html}"
   },
</#macro>

<#macro renderTags tags>
[<#list tags as x>"${x?j_string}"<#if x_has_next>, </#if></#list>]
</#macro>

<#macro addContent post>
   <#assign maxTextLength=512>
   <#if (! contentFormat??) || contentFormat == "" || contentFormat == "full">
      "content" : "${post.content?j_string}",
   <#elseif contentFormat == "htmlDigest">
      <#if (post.content?length > maxTextLength)>
         "content" : "${post.content?substring(0, maxTextLength)?j_string}",
      <#else>
         "content" : "${post.content?j_string}",
      </#if>
   <#elseif contentFormat == "textDigest">
      <#assign croppedTextContent=cropContent(post.properties.content, maxTextLength)>
      "content" : "${croppedTextContent?j_string}",
   <#else>
      <#-- no content returned -->
   </#if>
</#macro>

<#macro postJSON postData>
{
   <@postDataJSON postData=postData />
}
</#macro>

<#macro postDataJSON postData>
   <#-- which node should be used for urls? which for the post data? -->
   <#if postData.isTopicPost>
      <#assign refNode=postData.topic />
   <#else>
      <#assign refNode=postData.post />
   </#if>
   
   <#assign post=postData.post />

   <#-- render topic post only data first -->
   <#if postData.isTopicPost>
      "name" : "${postData.topic.name?html?js_string}",
      "totalReplyCount" : ${postData.totalReplyCount?c},
      <#if postData.lastReply??>
         "lastReplyOn" : "${postData.lastReply.properties.created?string("MMM dd yyyy HH:mm:ss 'GMT'Z '('zzz')'")}",
         <@renderPerson person=postData.lastReplyBy fieldName="lastReplyBy" />
      </#if>
      "tags" : <@renderTags tags=postData.tags />,
   </#if>

   <#-- data using refNode which might be the topic or the post node -->
   "url" : "/forum/post/node/${refNode.nodeRef.storeRef.protocol}/${refNode.nodeRef.storeRef.identifier}/${refNode.nodeRef.id}",
   "repliesUrl" : "/forum/post/node/${refNode.nodeRef.storeRef.protocol}/${refNode.nodeRef.storeRef.identifier}/${refNode.nodeRef.id}/replies",
   "nodeRef" : "${refNode.nodeRef?j_string}",
   
   <#-- normal data, the post node will used to fetch it -->
   "title" : "${(post.properties.title!"")?html?j_string}",
   "createdOn" : "${post.properties.created?string("MMM dd yyyy HH:mm:ss 'GMT'Z '('zzz')'")}",
   "modifiedOn" : "${post.properties.modified?string("MMM dd yyyy HH:mm:ss 'GMT'Z '('zzz')'")}",
   "isUpdated" : ${post.hasAspect("cm:contentupdated")?string},
   <#if (post.hasAspect("cm:contentupdated"))>
      "updatedOn" : "${post.properties["cm:contentupdatedate"]?string("MMM dd yyyy HH:mm:ss 'GMT'Z '('zzz')'")}",
   </#if>
   <#if postData.author??>
      <@renderPerson person=postData.author fieldName="author" />
   <#else>
      "author" : {
         "username" : "${post.properties["cm:creator"]}"
      },
   </#if>
   <@addContent post=post />
   "replyCount" : <#if post.sourceAssocs["cm:references"]??>${post.sourceAssocs["cm:references"]?size?c}<#else>0</#if>,
   "permissions" : { "edit": true, "delete" : true, "reply" : true }
</#macro>


<#-- Renders replies.
   The difference is to a normal post is that the children might be
   added inline in the returned JSON.
-->
<#macro repliesJSON data>
{
   <@postDataJSON postData=data />
   <#if data.children?exists>
      , "children": <@repliesRootJSON children=data.children />
   </#if>
}
</#macro>

<#macro repliesRootJSON children>
[
   <#list children as child>
      <@repliesJSON data=child/>
      <#if child_has_next>,</#if>
   </#list>
]
</#macro>
