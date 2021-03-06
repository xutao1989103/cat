<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<a:config>
<res:useJs value="${res.js.local['jquery.validate.min.js']}" target="head-js" />
	<res:useJs value="${res.js.local['dependencyConfig.js']}" target="head-js" />
	<res:useJs value="${res.js.local['alarm_js']}" target="head-js" />
	<res:useCss value="${res.css.local['select2.css']}" target="head-css" />
	<res:useJs value="${res.js.local['select2.min.js']}" target="head-js" />
			<form name="thirdPartyConfigUpdate" id="form" method="post"
				action="${model.pageUri}?op=thirdPartyConfigUpdate">
				<table class="table table-striped table-condensed table-bordered  table-hover">
					<tr>
						<td style="width:60%">
						<textarea name="content" style="width:100%" rows="20" cols="150">${model.content}</textarea>
						</td>
						<td style="width:40%">
						<h4>1. HTTP </h4>
						<p><span class="text-danger">[url]</span>：监控的对象</p>
						<p><span class="text-danger">[type]</span>：<span class="text-danger">get</span> 或 <span class="text-danger">post</span></p>
						<p><span class="text-danger">[domain]</span>：依赖于该第三方的项目名，会向该项目组联系人发第三方告警</p>
						<p><span class="text-danger">[par]</span>：请求中包含的参数，<span class="text-danger">id</span>为参数名称，<span class="text-danger">value</span>为参数值</p>
						<p>例如：
<xmp style="width:auto"><http url="http://cat.dp:8080" type="get" domain="Cat">
  <par id="domain" value="Cat"/>
  <par id="date" value="2014073111"/>
</http>
</xmp>
						</p>
						<br/>所有标红部分均为小写。
						
						</td>
					</tr>
					<tr>
						<td style="text-align:center" colspan="2"><input class='btn btn-primary btn-sm' 
							type="submit" name="submit" value="提交" /></td>
					</tr>
				</table>
			</form>
			<h4 class="text-center text-danger" id="state">&nbsp;</h4>
</a:config>
<script type="text/javascript">
		$(document).ready(function() {
			$('#application_config').addClass('active open');
			$('#thirdPartyConfigUpdate').addClass('active');
			var state = '${model.opState}';
			if(state=='Success'){
				$('#state').html('操作成功');
			}else{
				$('#state').html('操作失败');
			}
			setInterval(function(){
				$('#state').html('&nbsp;');
			},3000);
		});
</script>