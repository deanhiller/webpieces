<?php

header( "Content-type:text/html; charset=utf-8" );
require_once( "send_email_class.php" );
//print_r($_POST);die();

	$smtpserver = "smtp.qq.com";//SMTP服务器
$smtpserverport =25;//SMTP服务器端口
$smtpusermail = "22112868@qq.com";//SMTP服务器的用户邮箱
$smtpemailto = "china@usaboardingschools.org,laura@e-marketing-china.com";//发送给谁

$smtpuser = "22112868@qq.com";//SMTP服务器的用户帐号
$smtppass = "Emarketing2013";//SMTP服务器的用户密码
$mailsubject = "Inquiry from lp free1";//邮件主题
$mailbody = "你好!客户".$_POST['clientname']."的邮箱:".$_POST['clientemail']."，QQ/手机:".$_POST['clientphone']."，问题:".$_POST['clientmessage']." 请及时与客户联系！";//邮件内容
$mailtype = "TXT";//邮件格式（HTML/TXT）,TXT为文本邮件
$smtp = new smtp($smtpserver,$smtpserverport,true,$smtpuser,$smtppass);
$smtp->debug = false;//是否显示发送的调试信息
$smtp->sendmail($smtpemailto, $smtpusermail, $mailsubject, $mailbody, $mailtype);

echo"<script>window.location.href='/ranking/ok.html';</script>";
	
	


?>


