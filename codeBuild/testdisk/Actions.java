/*
 * LoadRunner Java script. (Build: 3020)
 * 
 * Script Description: 
 *     {
		    ret:boolean
		    error:string
		    data:object
		}                
 */

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;
import java.util.Set;
import java.util.Map;
import java.util.UUID;
import lrapi.lr;
import lrapi.web;
/**
 * 所有 接口 返回值格式是统一的，
 * 返回值数据格式
 *  {
	    ret:boolean
	    error:string
	    data:object
	}  
 * @author dell2
 *
 */
public class Actions
{
	//param for system
	String LAST="LAST";
	int FAILED=-1;
	int SUCC=0;
	//param for app
	boolean debug = true ;
	String volumeId;
	String createVolumeBody;
	String deleteVolumeBody;
	String user ;//= p.getProperty("user");
	String pass ;//= p.getProperty("pass");
	//request base url
	String reqUrl;
	private int caseInterval = 0 ; //测试用例之间的停顿时间
	private HashMap  sceneMap = new HashMap() ;
	private HashMap  dynamicParameters = new HashMap() ; //动态参数
	private HashMap statics = new HashMap() ; //statics
	private String file ;
	
	public int init() throws Throwable {
		//load app config
		load_conf();
		//testcase_fuwu_update.properties
		parseCaseConfig( file ) ;
		//login req
    	login();
    	//perpare param that use in action req
    	init_param();
    	
    	//if no error , return success, otherwise throw err
		return SUCC;
	}//end of init

	//test case
	//if async, need to use polling model
	public int action() throws Throwable {
		
		int ret = testChain();
		System.out.println(ret);
		return ret;
	}//end of action
	
	/**
	 * 测试链，处理多条测试用例，通过配置文件读取信息然后处理接口返回值
	 */
	public int testChain(){
		Iterator iter =this.sceneMap.keySet().iterator() ;
		int isSucc = SUCC;
		while(iter.hasNext()){
			String key = (String)iter.next() ;
			lr.set_transaction(key,0,lr.FAIL) ;
			statics.put(key,"failed") ;
			try {
				 List caseList = composeScene( key ) ;
				 loop( caseList );
				 lr.set_transaction(key,0,lr.PASS) ;
				 statics.put(key,"succeed") ;
				 String pass = "Scene "+key+" pass ...";
				 prt( pass ) ;
			} catch (Exception e) {
				System.out.println("case list");
				handlerCaseFail(e,  key );
				isSucc = FAILED;
				break;
			} 
			//场景间的间隔时间
			caseInterval() ;
		}
		
		 printStatics() ;
		 return isSucc;
		
	}
	
	public void printStatics(){
		Iterator iter =this.statics.keySet().iterator() ;
		System.out.println(" ") ;
		System.out.println(" ") ;
		System.out.println("-------statics-------") ;
		while(iter.hasNext()){
			String key = (String)iter.next() ;
			String value = (String)statics.get(key ) ;
			System.out.println("Scene:"+key+" "+value) ;
		}
	}
	
	/**
	 * 停顿时间
	 */
	@SuppressWarnings("static-access")
	private void caseInterval(){
		if( this.caseInterval > 0 ){
			try {
				Thread.currentThread().sleep( this.caseInterval  * 1000 );
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void handlerCaseFail(Exception e,String scene){
		lr.set_transaction(scene,0,lr.FAIL) ;
		statics.put(scene,"failed ->"+e.getMessage()) ;
		//System.out.println("Scene:"+scene+"failed of "+e.getMessage() ) ;
		//e.printStackTrace();
	}
	
	public void parseCaseConfig(String fileName) throws Exception{
		if( fileName != null && !"".equals( fileName ) ){
			InputStream in  = this.getClass().getResourceAsStream( fileName ) ;
			if( in == null  ){
				throw new NullPointerException("No scene configuration exist,"+fileName) ;
			}
			Properties pro = new Properties();   
			pro.load( in );
			parseSceneProperties(pro);
			
		}else{
			throw new Exception("No configure file") ;
		}
	}
	
	private void parseSceneProperties(Properties pro) throws Exception{
		if( null == pro ){
			throw new NullPointerException() ;
		}
		
		if( sceneMap == null ){
			sceneMap = new HashMap() ;
		}
		
		Set keys = pro.keySet() ;
		Iterator  iter = keys.iterator() ;
		int counter = 0 ;
		
		while(iter.hasNext()){
			String key = (String) iter.next() ;
			String value = pro.getProperty( key ) ;
			 
			//scene  case  property
			//xx.name=  such as createservice.createApplication.name
			String serviceKey = key.substring(0, key.lastIndexOf('.')  ) ;
			String sceneName = serviceKey.substring(0, serviceKey.lastIndexOf('.')  ) ;
			String caseName = serviceKey.substring(  serviceKey.lastIndexOf('.')+1  ) ;
			String property = key.substring(  key.lastIndexOf('.')+1  ) ;
			HashMap  scene = (HashMap) sceneMap.get( sceneName  ) ;
			if( scene == null ){
				scene = new HashMap<String, TestCaseConfig>() ;
			}
			
			TestCaseConfig testCase = (TestCaseConfig) scene.get( caseName ) ;
			
			if( null == testCase ){
				testCase = new TestCaseConfig() ;
			}
			
			if( "name".equalsIgnoreCase( property )){
				testCase.setId( value );
				if( testCase.getOrder() == -1 ){
					testCase.setOrder( counter++); //设置默认的序号
				}
			}else if(property.contains( "method" )){
				testCase.setMethod(value);
			}else if( "url".equalsIgnoreCase( property ) ){
				testCase.setUrl( value );
			}else if(  property.contains( "data" ) ){
				testCase.addOpt( value );
			}else if( "order".equalsIgnoreCase( property )){
				if( value != null && !"".equals( value.trim() )){
					try {
						testCase.setOrder( Integer.parseInt( value ));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}else if( "validateClass".equalsIgnoreCase( property ) ){
				testCase.setValidateClass( value );
			}else{
				//System.out.println("Useless property "+key+":"+value  );
			}
			
			if( "interval".equalsIgnoreCase( property )){
				try {
					int interval = Integer.parseInt( value.trim() ) ;
					testCase.setInterval( interval );
				} catch (Exception e) {
					e.printStackTrace();
				}
			} 
			
			scene.put(caseName , testCase) ;
			sceneMap.put( sceneName, scene) ;
		}
		
	}
	
	private List  composeScene(String scene)  throws Exception{
		List  caseList = new ArrayList () ;
		
		if( null != scene ){
			HashMap  scenery = (HashMap) this.sceneMap.get( scene) ;
			if( null == scenery ){
				throw new NullPointerException("no senery :"+scene) ;
			}
			Iterator iter = scenery.keySet().iterator() ;
			while( iter.hasNext() ){
				String caseName = (String)iter.next() ;
				TestCaseConfig testCase = (TestCaseConfig)scenery.get( caseName ) ;
				
				if ( null != testCase ) {
					caseList.add(testCase ) ;
				}
			}
			
		}
		sort(caseList, new Comparator() {

			@Override
			public int compare(Object o1, Object o2) {
				TestCaseConfig t1 = (TestCaseConfig)o1 ;
				TestCaseConfig t2 = (TestCaseConfig)o2 ;
				
				return t1.getOrder() - t2.getOrder()  ;
			}
		});
		return caseList ;
	}
	
	private void sort( List  caseList,Comparator c) {
        Object[] a = caseList.toArray();
        Arrays.sort(a, (Comparator) c);
        ListIterator  i = caseList.listIterator();
        for (Object e : a) {
            i.next();
            i.set((TestCaseConfig) e);
        }
    }
	
	/**
	 * 循环处理测试用例
	 * @param cases
	 */
	public void loop(List  cases) throws Exception{
		
		if( null != cases && cases.size() > 0){
			for(int i=0;i< cases.size() ;i++){
				try {
					TestCaseConfig caze = (TestCaseConfig) cases.get( i ) ;
					
					int ret = test( caze ) ;
//					System.out.println("ret = 9999");
					if( ret == FAILED ){
						throw new Exception("Case "+caze.getId() +" failed" ) ;
					}
					System.out.println( "order:{"+caze.getOrder()+"}" ) ;
					// 操作间的间隔
					if( caze.getInterval() != 0 ){
						sleep(caze.getInterval() ) ;
					}
				} catch (Exception e) {
					//e.printStackTrace();
					String message = e.getMessage() ;
					throw new Exception( message) ;
				}
			}
		}
	}
	
	
	/**
	 * 测试的主要逻辑
	 */
	public int test(TestCaseConfig cass) throws Exception{
		TestCaseValidater validate = getTestCaseValidater(cass) ;
	
		return doTest( cass.getId() ,  cass.getUrl(), cass.getMethod(), validate,  cass.getOpts() ) ;
    }
	
	public static TestCaseValidater getTestCaseValidater(TestCaseConfig cass){
		TestCaseValidater validate = null ;
		
		if( cass.getValidateClass() != null ){
			try {
				Class classType = Class.forName( cass.getValidateClass());  
				validate = (TestCaseValidater) classType.newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}  
		}
	   return validate ;
	}
	
	public int doTest(String id,String url, String method, TestCaseValidater validate, Object ...opts) throws Exception{
		  int _ret= SUCC;
		 //  {
		//		    ret:boolean
		//		    error:string
		//		    data:object
		//		}
		  String ret = "" ;
		  web.set_max_html_param_len("2048") ;
		  web_reg_save_param("ResponseBody", "LB={\"ret\":", "RB=,\"error\":", "Search=Body", "LAST" );
		  //web_reg_save_param("ret", "LB=\"error\":", "RB=,\"data\"", "Search=Body", "LAST" ); //输出整个返回值
		  web.reg_save_param("ResponseBodying", new String[]{"LB=", "RB=", "Search=Body", "LAST"});
		  String[] re = new String[]{
		  	"Resoure=0",
		  	"RecContentType=application/json",
		  	"Mode=HTML",
		  	"EncType=application/json;charset=UTF-8"
		  };
		  System.out.println("method:"+method);
		  if("get".equals(method)){
		  	commonGet(id, url, obj2Str(opts));
		  }else{
		  	commonPost(id , url ,  obj2Str(opts) );	
		  }
			
			//do
			sleep(5) ;
			/**
			 *  check create storage return fielad 
			 *  1.true  success
			 *  2.false failed
			 */
			String result = lr_eval_string("{ResponseBody}");
			String resulting = lr_eval_string("{ResponseBodying}");
			System.out.println("this"+resulting);
			//String ret = lr_eval_string("{ret}");
			//prt(ret,result); // 打印输出
//			TestCreat testCreat = new TestCreat();
//			TestGetPod testPod = new TestGetPod();
//			TestVolumeStatus statue = new TestVolumeStatus();
//			TestVolume volume = new TestVolume();
//			System.out.println("this is "+validate);
			
			if( null != validate ){
//				System.out.println("validate is not null ");
				return validate.validate( resulting )?SUCC:FAILED ;
			}
			if( !"true".equals(result.trim()) ){
			   _ret=FAILED;	
			   throw new Exception("Case "+id+" failed ->"+ret) ;
			  // System.out.println( "result:{"+result+"}" ) ;
			}
					
			return _ret;
		}

	/**
	 * 
	 * @param extOpts {
	 *   body,
	 *   referer,
	 *   ...
	 * }
	 */
	public int commonPost(String svc ,String url,String ...extOpts) throws Exception{
		String[] args  ;
		String referer = "Referer="+reqUrl+"/cp/service" ; //url=http://test.naturecloud.io
		String[] ops= new String[]{
				"Method=POST", 
				"Resource=0", 
				"RecContentType=text/html", 
				"Snapshot=t11.inf", 
				"Mode=HTML", 
				referer, 
				"EncType=application/json;charset=UTF-8"
		} ;
		
		
		if( extOpts !=null && extOpts.length > 0 ){
			args = new String[extOpts.length+ ops.length+1 ] ;
			copyFrom(args,ops,0, 0, ops.length) ;
			copyFrom(args,extOpts,ops.length , 0, extOpts.length) ;
		}else{
			args = new String[  ops.length+1 ] ;
			copyFrom(args,ops,0, 0, ops.length) ;
		}
		args[args.length -1 ] = LAST ;
		
		return web_custom_request(  svc ,  url ,args ) ;
	}
	
	public int commonGet(String svc ,String url,String ...extOpts) throws Exception{
		String[] args  ;
		String referer = "Referer="+reqUrl+"/cp/service" ; //url=http://test.naturecloud.io
		String[] ops= new String[]{
				"Method=GET", 
				"Resource=0", 
				"RecContentType=text/html", 
				"Snapshot=t99.inf", 
				"Mode=HTML", 
				referer, 
//				"EncType=application/json;charset=UTF-8"
		} ;
		
		
		if( extOpts !=null && extOpts.length > 0 ){
			args = new String[extOpts.length+ ops.length+1 ] ;
			copyFrom(args,ops,0, 0, ops.length) ;
			copyFrom(args,extOpts,ops.length , 0, extOpts.length) ;
		}else{
			args = new String[  ops.length+1 ] ;
			copyFrom(args,ops,0, 0, ops.length) ;
		}
		args[args.length -1 ] = LAST ;
		
		return web_custom_request(  svc ,  url ,args ) ;
	}
	
	public int web_custom_request(String service,String url,String ...args ) throws Exception{
		String[] opts = new String[0] ;
		
		if( args != null && args.length > 0 ){
			opts = Arrays.copyOf(args, args.length ) ;
		}
		if( debug ){
			lr_message( service ) ;
			lr_message( url ) ;
			lr_message( "options:" ) ;
			for(int i=0 ;i< opts.length ;i++){
				lr_message( opts[i] ) ;
			}
		}
		 try {
			return web.custom_request(service ,url, opts ) ;
		} catch ( Exception e) {
			throw e ;
		}
	}
	
	public String lr_eval_string( String p){
		return lr.eval_string( p ) ;
	}
	
	public void lr_message(String key ){
		lr.message( "[DEBUG]"+ key ) ;
	}
	
	public void lr_message(String key,String v){
		lr.message(key+v ) ;
	}
	
	public void web_reg_save_param(String para,String ...args){
		web.reg_save_param( para,args );
	}
	
	private void copyFrom(String[] targ,String[] ori,int targStart,int oriStart,int size){
		int j = oriStart ;
		for(int i=targStart ; i< (targStart+size) ; i++){
			String value =  ori[j++]  ;
			if(value.indexOf("#") != -1 && value.lastIndexOf("#")!=value.indexOf("#") ){
				//Body={\"sid\":\"#volumeId#\"}
				int first = value.indexOf("#") ;
				int last = value.lastIndexOf("#") ;
				String key = value.substring( first+1, last ) ;
				String dynamicvalue =  (String)this.dynamicParameters.get( key ) ;
				
				if( dynamicvalue != null ){
					value = value.replace( "#"+key+"#" , dynamicvalue  ) ;
				}
			}
			targ[i] = value ; //  
		}
	}
	
	public static String[] obj2Str(Object[] arr){
		if( arr ==null ){
			return null ;
		}
		String[] arrs = new String[arr.length] ;
		for(int i = 0 ;i< arr.length ;i++){
			arrs[i] = (String) arr[i] ;
		}
		return arrs ;
	}
	
//	--------------
	
	public void prt(String ...str){
		if( null != str && str.length > 0 ){
			for(int i=0;i< str.length;i++){
				System.out.println( str[i] ) ;
			}
		}
	}

	public int end() throws Throwable {
		//perpare release param
		init_end_param();
		//cleanup data
    	//release();
    	//return
		return SUCC;
	}//end of end
	
	/**
	 * 加载配置并且解析所有的测试用例额以及配置文件
	 */
	public void load_conf(){
		//TODO parse the test case 
    	InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("config.properties");
    	Properties p = new Properties();   
		try {   
			p.load(inputStream);
			reqUrl = p.getProperty("url");
			file = p.getProperty("file");
			user = p.getProperty("user");
			pass = p.getProperty("pass");
			
			String interval = p.getProperty("caseInterval");
			if( null != interval ){
				caseInterval = Integer.parseInt( interval ) ; //测试用例之间的停顿时间
			}
			
		} catch (IOException e1) {   
			e1.printStackTrace();   
		}   
    }
    
	//login request used in every case
	//maybe put in common class
    public void login() throws Throwable{
    	
		//also need to check if login successs
    	//web_reg_save_param("ret", "LB=", "RB=", "Search=Body", "LAST" ); //输
    	String body = "Body={\"username\":\""+user+"\",\"password\":\""+pass+"\"}";
    	
    	web.custom_request("authenticate", 
		"URL="+reqUrl+"/userBasicInfo/authenticate", new String[]{ 
		"Method=POST", 
		"Resource=0", 
		"RecContentType=text/html", 
		"Snapshot=t11.inf", 
		"Mode=HTML", 
		"EncType=application/json;charset=UTF-8", 
		body, 
		LAST});	
//    	String ret = lr_eval_string("{ret}");
//    	if( null == ret || "".equals( ret )){
//    		throw new NullPointerException("no session created,login failed") ;
//    	}
    	
    }
 
	//perpare create volume body
	//generator volumeId
    public void init_param(){
    	volumeId = UUID.randomUUID().toString();
    	volumeId = "test"+volumeId.substring(0,16) ;
    	dynamicParameters.put("volumeId", volumeId) ;
    }
    
	//perpare delete volume body
    public void init_end_param(){
    	deleteVolumeBody = "Body={\"volumeId\":\"fanjun-"+volumeId+"\"}";
    }
    
	//cleanup logic
    public void release() throws Throwable{
		sleep( 2 ) ;
    	web.custom_request("delstorage", 
		"URL="+reqUrl+"/apiStorage/delstorage", new String[]{ 
		"Method=POST", 
		"Resource=0", 
		"RecContentType=application/json", 
		"Snapshot=t11.inf", 
		"Mode=HTML", 
		"EncType=application/json;charset=UTF-8", 
		deleteVolumeBody,
		LAST});	
    
    }
	
	private void sleep(int sec){
    	try {
			Thread.currentThread().sleep( sec * 1000 );
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
}

 class TestCaseConfig{
	@SuppressWarnings("unused")
	private int order ;
	private String id ;
	private String url ;
	private String method;
	private ArrayList  opts = new ArrayList();
	private String validateClass ;
	private int interval = 0 ;
	
	public TestCaseConfig(String id ,String url,int order,String method){
		this.id = id ;
		this.url = url ;
		this.order = order ;
		this.method = method;
	}
	
	public TestCaseConfig(String id ,String url,String method){
		this(  id ,  url, -1, method) ;
	}
	
	public TestCaseConfig(){
		this(  "" ,  "",-1 , "") ;
	}
	
	public void addOpt(String opt){
		if( null != opt && !"".equals( opt.trim() )){
			opts.add( opt ) ;
		}
	}
	
	public Object[] getOpts(){
		return  this.opts.toArray() ;
	}
	
	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getMethod(){
		return method;
	}
	
	public void setMethod(String method){
		this.method = method;
	}
	
	public String getValidateClass() {
		return validateClass;
	}

	public void setValidateClass(String validateClass) {
		this.validateClass = validateClass;
	}
	
	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}
}

interface TestCaseValidater {
	/**
	 * 验证接口返回值是否正确，通过反射的方式调用
	 * @param bundle
	 * @return
	 */
	public boolean validate(Map<String, Object> bundle) ;
	
	public boolean validate( String ret) ;
}

class Compare implements Comparator<TestCaseConfig>{
    @Override
	public int compare(TestCaseConfig o1, TestCaseConfig o2) {
		return o1.getOrder() - o2.getOrder() ;
	}
}