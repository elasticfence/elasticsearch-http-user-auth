package org.elasticsearch.plugin.priv.data;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.plugin.http.user.auth.UserAuthenticator;
import org.elasticsearch.plugin.http.user.auth.data.UserData;
import org.junit.Test;


public class AuthUserTest {

	@Test
	public void authTest() {
        SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		try {
			System.out.println(sdf.parse("2015-09-06T03:06:36.458+09:00"));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		UserAuthenticator.setRootPassword("");
		UserAuthenticator.reloadUserDataCache(null);
		UserAuthenticator userAuth ;
		
		List<UserData> userDataList = Lists.newArrayList();

		userDataList = Lists.newArrayList();
		userDataList.add(UserData.restoreFromESData("test_admin", UserData.encPassword("test_password"), "/"));
		UserAuthenticator.reloadUserDataCache(userDataList);
		userAuth = new UserAuthenticator("test_admin", "test_password");
		assertTrue (userAuth.execAuth("/"));
		assertFalse(userAuth.execAuth("/*"));
		assertFalse(userAuth.execAuth("/test_index"));
		userAuth = new UserAuthenticator("test_admin", "test_password1");
		assertFalse(userAuth.execAuth("/"));
		assertFalse(userAuth.execAuth("/*"));
		assertFalse(userAuth.execAuth("/test_index"));
		userAuth = new UserAuthenticator("test_admin1", "test_password");
		assertFalse(userAuth.execAuth("/"));
		assertFalse(userAuth.execAuth("/*"));
		assertFalse(userAuth.execAuth("/test_index"));
		userAuth = new UserAuthenticator("test_admin1", "test_password1");
		assertFalse(userAuth.execAuth("/"));
		assertFalse(userAuth.execAuth("/*"));
		assertFalse(userAuth.execAuth("/test_index"));

		userDataList = Lists.newArrayList();
		userDataList.add(UserData.restoreFromESData("test_admin", UserData.encPassword("test_password"), "/test_index"));
		UserAuthenticator.reloadUserDataCache(userDataList);
		userAuth = new UserAuthenticator("test_admin", "test_password");
		assertFalse(userAuth.execAuth("/"));
		assertFalse(userAuth.execAuth("/*"));
		assertTrue (userAuth.execAuth("/test_index"));
		assertFalse(userAuth.execAuth("/test_index1"));
		userAuth = new UserAuthenticator("test_admin", "test_password1");
		assertFalse(userAuth.execAuth("/"));
		assertFalse(userAuth.execAuth("/*"));
		assertFalse(userAuth.execAuth("/test_index"));
		assertFalse(userAuth.execAuth("/test_index1"));
		userAuth = new UserAuthenticator("test_admin1", "test_password");
		assertFalse(userAuth.execAuth("/"));
		assertFalse(userAuth.execAuth("/*"));
		assertFalse(userAuth.execAuth("/test_index"));
		assertFalse(userAuth.execAuth("/test_index1"));
		userAuth = new UserAuthenticator("test_admin1", "test_password1");
		assertFalse(userAuth.execAuth("/"));
		assertFalse(userAuth.execAuth("/*"));
		assertFalse(userAuth.execAuth("/test_index"));
		assertFalse(userAuth.execAuth("/test_index1"));

		userDataList = Lists.newArrayList();
		userDataList.add(UserData.restoreFromESData("test_admin", UserData.encPassword("test_password"), "/test_index*"));
		UserAuthenticator.reloadUserDataCache(userDataList);
		userAuth = new UserAuthenticator("test_admin", "test_password");
		assertFalse(userAuth.execAuth("/"));
		assertTrue (userAuth.execAuth("/test_index"));
		assertTrue (userAuth.execAuth("/test_index1"));
		assertFalse(userAuth.execAuth("/test_1index"));
		assertFalse(userAuth.execAuth("/1test_index"));
		assertFalse(userAuth.execAuth("/*"));
		assertTrue (userAuth.execAuth("/test_index*"));
		assertFalse(userAuth.execAuth("/test_*index"));
		assertFalse(userAuth.execAuth("/*test_index"));

		userAuth = new UserAuthenticator("test_admin", "test_password1");
		assertFalse(userAuth.execAuth("/"));
		assertFalse(userAuth.execAuth("/test_index"));
		assertFalse(userAuth.execAuth("/test_index1"));
		assertFalse(userAuth.execAuth("/test_1index"));
		assertFalse(userAuth.execAuth("/1test_index"));
		assertFalse(userAuth.execAuth("/*"));
		assertFalse(userAuth.execAuth("/test_index*"));
		assertFalse(userAuth.execAuth("/test_*index"));
		assertFalse(userAuth.execAuth("/*test_index"));

		userAuth = new UserAuthenticator("test_admin1", "test_password");
		assertFalse(userAuth.execAuth("/"));
		assertFalse(userAuth.execAuth("/test_index"));
		assertFalse(userAuth.execAuth("/test_index1"));
		assertFalse(userAuth.execAuth("/test_1index"));
		assertFalse(userAuth.execAuth("/1test_index"));
		assertFalse(userAuth.execAuth("/*"));
		assertFalse(userAuth.execAuth("/test_index*"));
		assertFalse(userAuth.execAuth("/test_*index"));
		assertFalse(userAuth.execAuth("/*test_index"));
		
		userAuth = new UserAuthenticator("test_admin1", "test_password1");
		assertFalse(userAuth.execAuth("/"));
		assertFalse(userAuth.execAuth("/test_index"));
		assertFalse(userAuth.execAuth("/test_index1"));
		assertFalse(userAuth.execAuth("/test_1index"));
		assertFalse(userAuth.execAuth("/1test_index"));
		assertFalse(userAuth.execAuth("/*"));
		assertFalse(userAuth.execAuth("/test_index*"));
		assertFalse(userAuth.execAuth("/test_*index"));
		assertFalse(userAuth.execAuth("/*test_index"));

		userAuth = new UserAuthenticator("", "");
		assertFalse(userAuth.execAuth("/"));
		assertFalse(userAuth.execAuth("/test_index"));
		assertFalse(userAuth.execAuth("/test_index1"));
		assertFalse(userAuth.execAuth("/test_1index"));
		assertFalse(userAuth.execAuth("/1test_index"));
		assertFalse(userAuth.execAuth("/*"));
		assertFalse(userAuth.execAuth("/test_index*"));
		assertFalse(userAuth.execAuth("/test_*index"));
		assertFalse(userAuth.execAuth("/*test_index"));

		userDataList = Lists.newArrayList();
		userDataList.add(UserData.restoreFromESData("test_admin", UserData.encPassword("test_password"), "/test_*index"));
		UserAuthenticator.reloadUserDataCache(userDataList);
		userAuth = new UserAuthenticator("test_admin", "test_password");
		assertFalse(userAuth.execAuth("/"));
		assertTrue (userAuth.execAuth("/test_index"));
		assertFalse(userAuth.execAuth("/test_index1"));
		assertTrue (userAuth.execAuth("/test_1index"));
		assertFalse(userAuth.execAuth("/1test_index"));
		assertFalse(userAuth.execAuth("/*"));
		assertFalse(userAuth.execAuth("/test_index*"));
		assertTrue (userAuth.execAuth("/test_*index"));
		assertFalse(userAuth.execAuth("/*test_index"));

		userAuth = new UserAuthenticator("", "");
		assertFalse(userAuth.execAuth("/"));
		assertFalse(userAuth.execAuth("/test_index"));
		assertFalse(userAuth.execAuth("/test_index1"));
		assertFalse(userAuth.execAuth("/test_1index"));
		assertFalse(userAuth.execAuth("/1test_index"));
		assertFalse(userAuth.execAuth("/*"));
		assertFalse(userAuth.execAuth("/test_index*"));
		assertFalse(userAuth.execAuth("/test_*index"));
		assertFalse(userAuth.execAuth("/*test_index"));

		userDataList = Lists.newArrayList();
		userDataList.add(UserData.restoreFromESData("test_admin", UserData.encPassword("test_password"), "/*test_index"));
		UserAuthenticator.reloadUserDataCache(userDataList);
		userAuth = new UserAuthenticator("test_admin", "test_password");
		assertFalse(userAuth.execAuth("/"));
		assertTrue (userAuth.execAuth("/test_index"));
		assertFalse(userAuth.execAuth("/test_index1"));
		assertFalse(userAuth.execAuth("/test_1index"));
		assertTrue (userAuth.execAuth("/1test_index"));
		assertFalse(userAuth.execAuth("/*"));
		assertFalse(userAuth.execAuth("/test_index*"));
		assertFalse(userAuth.execAuth("/test_*index"));
		assertTrue (userAuth.execAuth("/*test_index"));

		userAuth = new UserAuthenticator("", "");
		assertFalse(userAuth.execAuth("/"));
		assertFalse(userAuth.execAuth("/test_index"));
		assertFalse(userAuth.execAuth("/test_index1"));
		assertFalse(userAuth.execAuth("/test_1index"));
		assertFalse(userAuth.execAuth("/1test_index"));
		assertFalse(userAuth.execAuth("/*"));
		assertFalse(userAuth.execAuth("/test_index*"));
		assertFalse(userAuth.execAuth("/test_*index"));
		assertFalse(userAuth.execAuth("/*test_index"));

		userDataList = Lists.newArrayList();
		userDataList.add(UserData.restoreFromESData("test_admin", UserData.encPassword("test_password"), "/test_*index*"));
		UserAuthenticator.reloadUserDataCache(userDataList);
		userAuth = new UserAuthenticator("test_admin", "test_password");
		assertFalse(userAuth.execAuth("/"));
		assertTrue (userAuth.execAuth("/test_index"));
		assertTrue (userAuth.execAuth("/test_index1"));
		assertTrue (userAuth.execAuth("/test_1index"));
		assertFalse(userAuth.execAuth("/1test_index"));
		assertFalse(userAuth.execAuth("/*"));
		assertFalse(userAuth.execAuth("/test_index*"));
		assertFalse(userAuth.execAuth("/test_*index"));
		assertFalse(userAuth.execAuth("/*test_index"));
		assertTrue (userAuth.execAuth("/test_*index*"));
		assertFalse(userAuth.execAuth("/*test_*index"));
		assertFalse(userAuth.execAuth("/*test_index*"));
		assertFalse(userAuth.execAuth("/*test_*index*"));

		userAuth = new UserAuthenticator("", "");
		assertFalse(userAuth.execAuth("/"));
		assertFalse(userAuth.execAuth("/test_index"));
		assertFalse(userAuth.execAuth("/test_index1"));
		assertFalse(userAuth.execAuth("/test_1index"));
		assertFalse(userAuth.execAuth("/1test_index"));
		assertFalse(userAuth.execAuth("/*"));
		assertFalse(userAuth.execAuth("/test_index*"));
		assertFalse(userAuth.execAuth("/test_*index"));
		assertFalse(userAuth.execAuth("/*test_index"));
		assertFalse(userAuth.execAuth("/test_*index*"));
		assertFalse(userAuth.execAuth("/*test_*index"));
		assertFalse(userAuth.execAuth("/*test_index*"));
		assertFalse(userAuth.execAuth("/*test_*index*"));

		userDataList = Lists.newArrayList();
		userDataList.add(UserData.restoreFromESData("test_admin", UserData.encPassword("test_password"), "/*test_*index"));
		UserAuthenticator.reloadUserDataCache(userDataList);
		userAuth = new UserAuthenticator("test_admin", "test_password");
		assertFalse(userAuth.execAuth("/"));
		assertTrue (userAuth.execAuth("/test_index"));
		assertFalse(userAuth.execAuth("/test_index1"));
		assertTrue (userAuth.execAuth("/test_1index"));
		assertTrue (userAuth.execAuth("/1test_index"));
		assertFalse(userAuth.execAuth("/*"));
		assertFalse(userAuth.execAuth("/test_index*"));
		assertFalse(userAuth.execAuth("/test_*index"));
		assertFalse(userAuth.execAuth("/*test_index"));
		assertFalse(userAuth.execAuth("/test_*index*"));
		assertTrue (userAuth.execAuth("/*test_*index"));
		assertFalse(userAuth.execAuth("/*test_index*"));
		assertFalse(userAuth.execAuth("/*test_*index*"));

		userAuth = new UserAuthenticator("", "");
		assertFalse(userAuth.execAuth("/"));
		assertFalse(userAuth.execAuth("/test_index"));
		assertFalse(userAuth.execAuth("/test_index1"));
		assertFalse(userAuth.execAuth("/test_1index"));
		assertFalse(userAuth.execAuth("/1test_index"));
		assertFalse(userAuth.execAuth("/*"));
		assertFalse(userAuth.execAuth("/test_index*"));
		assertFalse(userAuth.execAuth("/test_*index"));
		assertFalse(userAuth.execAuth("/*test_index"));
		assertFalse(userAuth.execAuth("/test_*index*"));
		assertFalse(userAuth.execAuth("/*test_*index"));
		assertFalse(userAuth.execAuth("/*test_index*"));
		assertFalse(userAuth.execAuth("/*test_*index*"));

		userDataList = Lists.newArrayList();
		userDataList.add(UserData.restoreFromESData("test_admin", UserData.encPassword("test_password"), "/*test_index*"));
		UserAuthenticator.reloadUserDataCache(userDataList);
		userAuth = new UserAuthenticator("test_admin", "test_password");
		assertFalse(userAuth.execAuth("/"));
		assertTrue (userAuth.execAuth("/test_index"));
		assertTrue (userAuth.execAuth("/test_index1"));
		assertFalse(userAuth.execAuth("/test_1index"));
		assertTrue (userAuth.execAuth("/1test_index"));
		assertFalse(userAuth.execAuth("/*"));
		assertFalse(userAuth.execAuth("/test_index*"));
		assertFalse(userAuth.execAuth("/test_*index"));
		assertFalse(userAuth.execAuth("/*test_index"));
		assertFalse(userAuth.execAuth("/test_*index*"));
		assertFalse(userAuth.execAuth("/*test_*index"));
		assertTrue (userAuth.execAuth("/*test_index*"));
		assertFalse(userAuth.execAuth("/*test_*index*"));

		userAuth = new UserAuthenticator("", "");
		assertFalse(userAuth.execAuth("/"));
		assertFalse(userAuth.execAuth("/test_index"));
		assertFalse(userAuth.execAuth("/test_index1"));
		assertFalse(userAuth.execAuth("/test_1index"));
		assertFalse(userAuth.execAuth("/1test_index"));
		assertFalse(userAuth.execAuth("/*"));
		assertFalse(userAuth.execAuth("/test_index*"));
		assertFalse(userAuth.execAuth("/test_*index"));
		assertFalse(userAuth.execAuth("/*test_index"));
		assertFalse(userAuth.execAuth("/test_*index*"));
		assertFalse(userAuth.execAuth("/*test_*index"));
		assertFalse(userAuth.execAuth("/*test_index*"));
		assertFalse(userAuth.execAuth("/*test_*index*"));

		userDataList = Lists.newArrayList();
		userDataList.add(UserData.restoreFromESData("test_admin", UserData.encPassword("test_password"), "/*test_*index*"));
		UserAuthenticator.reloadUserDataCache(userDataList);
		userAuth = new UserAuthenticator("test_admin", "test_password");
		assertFalse(userAuth.execAuth("/"));
		assertTrue (userAuth.execAuth("/test_index"));
		assertTrue (userAuth.execAuth("/test_index1"));
		assertTrue (userAuth.execAuth("/test_1index"));
		assertTrue (userAuth.execAuth("/1test_index"));
		assertFalse(userAuth.execAuth("/*"));
		assertFalse(userAuth.execAuth("/test_index*"));
		assertFalse(userAuth.execAuth("/test_*index"));
		assertFalse(userAuth.execAuth("/*test_index"));
		assertFalse(userAuth.execAuth("/test_*index*"));
		assertFalse(userAuth.execAuth("/*test_*index"));
		assertFalse(userAuth.execAuth("/*test_index*"));
		assertTrue (userAuth.execAuth("/*test_*index*"));

		userAuth = new UserAuthenticator("", "");
		assertFalse(userAuth.execAuth("/"));
		assertFalse(userAuth.execAuth("/test_index"));
		assertFalse(userAuth.execAuth("/test_index1"));
		assertFalse(userAuth.execAuth("/test_1index"));
		assertFalse(userAuth.execAuth("/1test_index"));
		assertFalse(userAuth.execAuth("/*"));
		assertFalse(userAuth.execAuth("/test_index*"));
		assertFalse(userAuth.execAuth("/test_*index"));
		assertFalse(userAuth.execAuth("/*test_index"));
		assertFalse(userAuth.execAuth("/test_*index*"));
		assertFalse(userAuth.execAuth("/*test_*index"));
		assertFalse(userAuth.execAuth("/*test_index*"));
		assertFalse(userAuth.execAuth("/*test_*index*"));
	}
	

	@Test
	public void authTestWithRootPassword() {
		UserAuthenticator.setRootPassword("root_password");
		UserAuthenticator.reloadUserDataCache(null);
		UserAuthenticator userAuth;
		
		List<UserData> userDataList = Lists.newArrayList();

		userAuth = new UserAuthenticator("root", "root_password");
		assertTrue(userAuth.execAuth("/"));
		assertTrue(userAuth.execAuth("/test_index"));
		assertTrue(userAuth.execAuth("/test_index1"));
		assertTrue(userAuth.execAuth("/test_1index"));
		assertTrue(userAuth.execAuth("/1test_index"));
		assertTrue(userAuth.execAuth("/test_1index1"));
		assertTrue(userAuth.execAuth("/1test_1index"));
		assertTrue(userAuth.execAuth("/1test_index1"));
		assertTrue(userAuth.execAuth("/1test_1index1"));
		assertTrue(userAuth.execAuth("/*"));
		assertTrue(userAuth.execAuth("/test_index*"));
		assertTrue(userAuth.execAuth("/test_*index"));
		assertTrue(userAuth.execAuth("/*test_index"));
		assertTrue(userAuth.execAuth("/test_*index*"));
		assertTrue(userAuth.execAuth("/*test_*index"));
		assertTrue(userAuth.execAuth("/*test_index*"));
		assertTrue(userAuth.execAuth("/*test_*index*"));

		userDataList = Lists.newArrayList();
		userDataList.add(UserData.restoreFromESData("test_admin", UserData.encPassword("test_password"), "/"));
		UserAuthenticator.reloadUserDataCache(userDataList);
		userAuth = new UserAuthenticator("test_admin", "test_password");
		assertTrue (userAuth.execAuth("/"));
		assertFalse(userAuth.execAuth("/*"));
		assertFalse(userAuth.execAuth("/test_index"));
		userAuth = new UserAuthenticator("test_admin", "test_password1");
		assertFalse(userAuth.execAuth("/"));
		assertFalse(userAuth.execAuth("/*"));
		assertFalse(userAuth.execAuth("/test_index"));
		userAuth = new UserAuthenticator("test_admin1", "test_password");
		assertFalse(userAuth.execAuth("/"));
		assertFalse(userAuth.execAuth("/*"));
		assertFalse(userAuth.execAuth("/test_index"));
		userAuth = new UserAuthenticator("test_admin1", "test_password1");
		assertFalse(userAuth.execAuth("/"));
		assertFalse(userAuth.execAuth("/*"));
		assertFalse(userAuth.execAuth("/test_index"));

		userDataList = Lists.newArrayList();
		userDataList.add(UserData.restoreFromESData("test_admin", UserData.encPassword("test_password"), "/test_index"));
		UserAuthenticator.reloadUserDataCache(userDataList);
		userAuth = new UserAuthenticator("test_admin", "test_password");
		assertFalse(userAuth.execAuth("/"));
		assertFalse(userAuth.execAuth("/*"));
		assertTrue (userAuth.execAuth("/test_index"));
		assertFalse(userAuth.execAuth("/test_index1"));
		userAuth = new UserAuthenticator("test_admin", "test_password1");
		assertFalse(userAuth.execAuth("/"));
		assertFalse(userAuth.execAuth("/*"));
		assertFalse(userAuth.execAuth("/test_index"));
		assertFalse(userAuth.execAuth("/test_index1"));
		userAuth = new UserAuthenticator("test_admin1", "test_password");
		assertFalse(userAuth.execAuth("/"));
		assertFalse(userAuth.execAuth("/*"));
		assertFalse(userAuth.execAuth("/test_index"));
		assertFalse(userAuth.execAuth("/test_index1"));
		userAuth = new UserAuthenticator("test_admin1", "test_password1");
		assertFalse(userAuth.execAuth("/"));
		assertFalse(userAuth.execAuth("/*"));
		assertFalse(userAuth.execAuth("/test_index"));
		assertFalse(userAuth.execAuth("/test_index1"));

		userDataList = Lists.newArrayList();
		userDataList.add(UserData.restoreFromESData("test_admin", UserData.encPassword("test_password"), "/test_index*"));
		UserAuthenticator.reloadUserDataCache(userDataList);
		userAuth = new UserAuthenticator("test_admin", "test_password");
		assertFalse(userAuth.execAuth("/"));
		assertTrue (userAuth.execAuth("/test_index"));
		assertTrue (userAuth.execAuth("/test_index1"));
		assertFalse(userAuth.execAuth("/test_1index"));
		assertFalse(userAuth.execAuth("/1test_index"));
		assertFalse(userAuth.execAuth("/*"));
		assertTrue (userAuth.execAuth("/test_index*"));
		assertFalse(userAuth.execAuth("/test_*index"));
		assertFalse(userAuth.execAuth("/*test_index"));

		userAuth = new UserAuthenticator("test_admin", "test_password1");
		assertFalse(userAuth.execAuth("/"));
		assertFalse(userAuth.execAuth("/test_index"));
		assertFalse(userAuth.execAuth("/test_index1"));
		assertFalse(userAuth.execAuth("/test_1index"));
		assertFalse(userAuth.execAuth("/1test_index"));
		assertFalse(userAuth.execAuth("/*"));
		assertFalse(userAuth.execAuth("/test_index*"));
		assertFalse(userAuth.execAuth("/test_*index"));
		assertFalse(userAuth.execAuth("/*test_index"));

		userAuth = new UserAuthenticator("test_admin1", "test_password");
		assertFalse(userAuth.execAuth("/"));
		assertFalse(userAuth.execAuth("/test_index"));
		assertFalse(userAuth.execAuth("/test_index1"));
		assertFalse(userAuth.execAuth("/test_1index"));
		assertFalse(userAuth.execAuth("/1test_index"));
		assertFalse(userAuth.execAuth("/*"));
		assertFalse(userAuth.execAuth("/test_index*"));
		assertFalse(userAuth.execAuth("/test_*index"));
		assertFalse(userAuth.execAuth("/*test_index"));

		userAuth = new UserAuthenticator("test_admin1", "test_password1");
		assertFalse(userAuth.execAuth("/"));
		assertFalse(userAuth.execAuth("/test_index"));
		assertFalse(userAuth.execAuth("/test_index1"));
		assertFalse(userAuth.execAuth("/test_1index"));
		assertFalse(userAuth.execAuth("/1test_index"));
		assertFalse(userAuth.execAuth("/*"));
		assertFalse(userAuth.execAuth("/test_index*"));
		assertFalse(userAuth.execAuth("/test_*index"));
		assertFalse(userAuth.execAuth("/*test_index"));

		userAuth = new UserAuthenticator("", "");
		assertFalse(userAuth.execAuth("/"));
		assertFalse(userAuth.execAuth("/test_index"));
		assertFalse(userAuth.execAuth("/test_index1"));
		assertFalse(userAuth.execAuth("/test_1index"));
		assertFalse(userAuth.execAuth("/1test_index"));
		assertFalse(userAuth.execAuth("/*"));
		assertFalse(userAuth.execAuth("/test_index*"));
		assertFalse(userAuth.execAuth("/test_*index"));
		assertFalse(userAuth.execAuth("/*test_index"));

		userDataList = Lists.newArrayList();
		userDataList.add(UserData.restoreFromESData("test_admin", UserData.encPassword("test_password"), "/test_*index"));
		UserAuthenticator.reloadUserDataCache(userDataList);
		userAuth = new UserAuthenticator("test_admin", "test_password");
		assertFalse(userAuth.execAuth("/"));
		assertTrue (userAuth.execAuth("/test_index"));
		assertFalse(userAuth.execAuth("/test_index1"));
		assertTrue (userAuth.execAuth("/test_1index"));
		assertFalse(userAuth.execAuth("/1test_index"));
		assertFalse(userAuth.execAuth("/*"));
		assertFalse(userAuth.execAuth("/test_index*"));
		assertTrue (userAuth.execAuth("/test_*index"));
		assertFalse(userAuth.execAuth("/*test_index"));

		userAuth = new UserAuthenticator("", "");
		assertFalse(userAuth.execAuth("/"));
		assertFalse(userAuth.execAuth("/test_index"));
		assertFalse(userAuth.execAuth("/test_index1"));
		assertFalse(userAuth.execAuth("/test_1index"));
		assertFalse(userAuth.execAuth("/1test_index"));
		assertFalse(userAuth.execAuth("/*"));
		assertFalse(userAuth.execAuth("/test_index*"));
		assertFalse(userAuth.execAuth("/test_*index"));
		assertFalse(userAuth.execAuth("/*test_index"));

		userDataList = Lists.newArrayList();
		userDataList.add(UserData.restoreFromESData("test_admin", UserData.encPassword("test_password"), "/*test_index"));
		UserAuthenticator.reloadUserDataCache(userDataList);
		userAuth = new UserAuthenticator("test_admin", "test_password");
		assertFalse(userAuth.execAuth("/"));
		assertTrue (userAuth.execAuth("/test_index"));
		assertFalse(userAuth.execAuth("/test_index1"));
		assertFalse(userAuth.execAuth("/test_1index"));
		assertTrue (userAuth.execAuth("/1test_index"));
		assertFalse(userAuth.execAuth("/*"));
		assertFalse(userAuth.execAuth("/test_index*"));
		assertFalse(userAuth.execAuth("/test_*index"));
		assertTrue (userAuth.execAuth("/*test_index"));

		userAuth = new UserAuthenticator("", "");
		assertFalse(userAuth.execAuth("/"));
		assertFalse(userAuth.execAuth("/test_index"));
		assertFalse(userAuth.execAuth("/test_index1"));
		assertFalse(userAuth.execAuth("/test_1index"));
		assertFalse(userAuth.execAuth("/1test_index"));
		assertFalse(userAuth.execAuth("/*"));
		assertFalse(userAuth.execAuth("/test_index*"));
		assertFalse(userAuth.execAuth("/test_*index"));
		assertFalse(userAuth.execAuth("/*test_index"));

		userDataList = Lists.newArrayList();
		userDataList.add(UserData.restoreFromESData("test_admin", UserData.encPassword("test_password"), "/test_*index*"));
		UserAuthenticator.reloadUserDataCache(userDataList);
		userAuth = new UserAuthenticator("test_admin", "test_password");
		assertFalse(userAuth.execAuth("/"));
		assertTrue (userAuth.execAuth("/test_index"));
		assertTrue (userAuth.execAuth("/test_index1"));
		assertTrue (userAuth.execAuth("/test_1index"));
		assertFalse(userAuth.execAuth("/1test_index"));
		assertFalse(userAuth.execAuth("/*"));
		assertFalse(userAuth.execAuth("/test_index*"));
		assertFalse(userAuth.execAuth("/test_*index"));
		assertFalse(userAuth.execAuth("/*test_index"));
		assertTrue (userAuth.execAuth("/test_*index*"));
		assertFalse(userAuth.execAuth("/*test_*index"));
		assertFalse(userAuth.execAuth("/*test_index*"));
		assertFalse(userAuth.execAuth("/*test_*index*"));

		userAuth = new UserAuthenticator("", "");
		assertFalse(userAuth.execAuth("/"));
		assertFalse(userAuth.execAuth("/test_index"));
		assertFalse(userAuth.execAuth("/test_index1"));
		assertFalse(userAuth.execAuth("/test_1index"));
		assertFalse(userAuth.execAuth("/1test_index"));
		assertFalse(userAuth.execAuth("/*"));
		assertFalse(userAuth.execAuth("/test_index*"));
		assertFalse(userAuth.execAuth("/test_*index"));
		assertFalse(userAuth.execAuth("/*test_index"));
		assertFalse(userAuth.execAuth("/test_*index*"));
		assertFalse(userAuth.execAuth("/*test_*index"));
		assertFalse(userAuth.execAuth("/*test_index*"));
		assertFalse(userAuth.execAuth("/*test_*index*"));

		userDataList = Lists.newArrayList();
		userDataList.add(UserData.restoreFromESData("test_admin", UserData.encPassword("test_password"), "/*test_*index"));
		UserAuthenticator.reloadUserDataCache(userDataList);
		userAuth = new UserAuthenticator("test_admin", "test_password");
		assertFalse(userAuth.execAuth("/"));
		assertTrue (userAuth.execAuth("/test_index"));
		assertFalse(userAuth.execAuth("/test_index1"));
		assertTrue (userAuth.execAuth("/test_1index"));
		assertTrue (userAuth.execAuth("/1test_index"));
		assertFalse(userAuth.execAuth("/test_1index1"));
		assertTrue (userAuth.execAuth("/1test_1index"));
		assertFalse(userAuth.execAuth("/1test_index1"));
		assertFalse(userAuth.execAuth("/1test_1index1"));
		assertFalse(userAuth.execAuth("/*"));
		assertFalse(userAuth.execAuth("/test_index*"));
		assertFalse(userAuth.execAuth("/test_*index"));
		assertFalse(userAuth.execAuth("/*test_index"));
		assertFalse(userAuth.execAuth("/test_*index*"));
		assertTrue (userAuth.execAuth("/*test_*index"));
		assertFalse(userAuth.execAuth("/*test_index*"));
		assertFalse(userAuth.execAuth("/*test_*index*"));

		userAuth = new UserAuthenticator("", "");
		assertFalse(userAuth.execAuth("/"));
		assertFalse(userAuth.execAuth("/test_index"));
		assertFalse(userAuth.execAuth("/test_index1"));
		assertFalse(userAuth.execAuth("/test_1index"));
		assertFalse(userAuth.execAuth("/1test_index"));
		assertFalse(userAuth.execAuth("/test_1index1"));
		assertFalse(userAuth.execAuth("/1test_1index"));
		assertFalse(userAuth.execAuth("/1test_index1"));
		assertFalse(userAuth.execAuth("/1test_1index1"));
		assertFalse(userAuth.execAuth("/*"));
		assertFalse(userAuth.execAuth("/test_index*"));
		assertFalse(userAuth.execAuth("/test_*index"));
		assertFalse(userAuth.execAuth("/*test_index"));
		assertFalse(userAuth.execAuth("/test_*index*"));
		assertFalse(userAuth.execAuth("/*test_*index"));
		assertFalse(userAuth.execAuth("/*test_index*"));
		assertFalse(userAuth.execAuth("/*test_*index*"));

		userDataList = Lists.newArrayList();
		userDataList.add(UserData.restoreFromESData("test_admin", UserData.encPassword("test_password"), "/*test_index*"));
		UserAuthenticator.reloadUserDataCache(userDataList);
		userAuth = new UserAuthenticator("test_admin", "test_password");
		assertFalse(userAuth.execAuth("/"));
		assertTrue (userAuth.execAuth("/test_index"));
		assertTrue (userAuth.execAuth("/test_index1"));
		assertFalse(userAuth.execAuth("/test_1index"));
		assertTrue (userAuth.execAuth("/1test_index"));
		assertFalse(userAuth.execAuth("/test_1index1"));
		assertFalse(userAuth.execAuth("/1test_1index"));
		assertTrue (userAuth.execAuth("/1test_index1"));
		assertFalse(userAuth.execAuth("/1test_1index1"));
		assertFalse(userAuth.execAuth("/*"));
		assertFalse(userAuth.execAuth("/test_index*"));
		assertFalse(userAuth.execAuth("/test_*index"));
		assertFalse(userAuth.execAuth("/*test_index"));
		assertFalse(userAuth.execAuth("/test_*index*"));
		assertFalse(userAuth.execAuth("/*test_*index"));
		assertTrue (userAuth.execAuth("/*test_index*"));
		assertFalse(userAuth.execAuth("/*test_*index*"));

		userAuth = new UserAuthenticator("", "");
		assertFalse(userAuth.execAuth("/"));
		assertFalse(userAuth.execAuth("/test_index"));
		assertFalse(userAuth.execAuth("/test_index1"));
		assertFalse(userAuth.execAuth("/test_1index"));
		assertFalse(userAuth.execAuth("/1test_index"));
		assertFalse(userAuth.execAuth("/*"));
		assertFalse(userAuth.execAuth("/test_index*"));
		assertFalse(userAuth.execAuth("/test_*index"));
		assertFalse(userAuth.execAuth("/*test_index"));
		assertFalse(userAuth.execAuth("/test_*index*"));
		assertFalse(userAuth.execAuth("/*test_*index"));
		assertFalse(userAuth.execAuth("/*test_index*"));
		assertFalse(userAuth.execAuth("/*test_*index*"));

		userDataList = Lists.newArrayList();
		userDataList.add(UserData.restoreFromESData("test_admin", UserData.encPassword("test_password"), "/*test_*index*"));
		UserAuthenticator.reloadUserDataCache(userDataList);
		userAuth = new UserAuthenticator("test_admin", "test_password");
		assertFalse(userAuth.execAuth("/"));
		assertTrue (userAuth.execAuth("/test_index"));
		assertTrue (userAuth.execAuth("/test_index1"));
		assertTrue (userAuth.execAuth("/test_1index"));
		assertTrue (userAuth.execAuth("/1test_index"));
		assertFalse(userAuth.execAuth("/*"));
		assertFalse(userAuth.execAuth("/test_index*"));
		assertFalse(userAuth.execAuth("/test_*index"));
		assertFalse(userAuth.execAuth("/*test_index"));
		assertFalse(userAuth.execAuth("/test_*index*"));
		assertFalse(userAuth.execAuth("/*test_*index"));
		assertFalse(userAuth.execAuth("/*test_index*"));
		assertTrue (userAuth.execAuth("/*test_*index*"));

		userAuth = new UserAuthenticator("", "");
		assertFalse(userAuth.execAuth("/"));
		assertFalse(userAuth.execAuth("/test_index"));
		assertFalse(userAuth.execAuth("/test_index1"));
		assertFalse(userAuth.execAuth("/test_1index"));
		assertFalse(userAuth.execAuth("/1test_index"));
		assertFalse(userAuth.execAuth("/*"));
		assertFalse(userAuth.execAuth("/test_index*"));
		assertFalse(userAuth.execAuth("/test_*index"));
		assertFalse(userAuth.execAuth("/*test_index"));
		assertFalse(userAuth.execAuth("/test_*index*"));
		assertFalse(userAuth.execAuth("/*test_*index"));
		assertFalse(userAuth.execAuth("/*test_index*"));
		assertFalse(userAuth.execAuth("/*test_*index*"));
	}
	
	
}
