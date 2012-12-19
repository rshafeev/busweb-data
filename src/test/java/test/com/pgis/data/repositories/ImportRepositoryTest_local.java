package test.com.pgis.data.repositories;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.pgis.bus.data.DBConnectionFactory;
import com.pgis.bus.data.models.ImportRouteModel;
import com.pgis.bus.data.orm.City;
import com.pgis.bus.data.orm.ImportObject;
import com.pgis.bus.data.orm.Route;
import com.pgis.bus.data.repositories.ICitiesRepository;
import com.pgis.bus.data.repositories.IRoutesRepository;
import com.pgis.bus.data.repositories.IimportRepository;
import com.pgis.bus.data.repositories.RepositoryException;
import com.pgis.bus.data.repositories.impl.CitiesRepository;
import com.pgis.bus.data.repositories.impl.ImportRepository;
import com.pgis.bus.data.repositories.impl.RoutesRepository;

import test.com.pgis.data.FileManager;
import test.com.pgis.data.TestDBConnectionManager;
import test.com.pgis.data.TestDataSource;

public class ImportRepositoryTest_local {

	@Before
	public void init() {
		TestDataSource source = new TestDataSource();
		TestDBConnectionManager dbConnectionManager = new TestDBConnectionManager(
				source.getDataSource());
		DBConnectionFactory.init(dbConnectionManager);
		System.out.print("init test\n");
	}

	@After
	public void destroy() {
		DBConnectionFactory.free();
	}

	@Test(expected = RepositoryException.class)
	public void insrtRouteTest() throws RepositoryException {
		System.out.println("insrtRouteTest()...");
		String data = FileManager.getFileData(FileManager.getTestResourcePath()
				+ "route_import_insert.dat");
		System.out.println(data);

		ImportRouteModel importModel = (new Gson()).fromJson(data,
				ImportRouteModel.class);
		System.out.println(importModel.toString());
		Route newRoute = null;
		try {
			newRoute = importModel.toRoute();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Connection c = DBConnectionFactory.getConnection();
		ICitiesRepository cities = new CitiesRepository(c, false, false);
		City city = cities.getCityByKey("kyiv");
		newRoute.setCity_id(city.id);
		IRoutesRepository routes = new RoutesRepository(c, false, false);
		routes.insertRoute(newRoute);

		DBConnectionFactory.closeConnection(c);
	}

	/**
	 * Вытавим импорт. объект А, а затем попытаемся получить его с БД (объект Б)
	 * Затем сверим А и Б. Они должны быть идентичны
	 * 
	 * @throws Exception
	 */
	@Test
	public void getObjectTest() throws Exception {

		System.out.println("getRouteTest()...");

		// load data
		String data = FileManager.getFileData(FileManager.getTestResourcePath()
				+ "route_import_insert.dat");

		// create repositories
		Connection c = DBConnectionFactory.getConnection();
		IimportRepository imports = new ImportRepository(c, false, false);

		// create importObject
		ImportObject importObject = new ImportObject();
		importObject.city_key = "kyiv";
		importObject.route_number = "test1_number1111";
		importObject.route_type = "c_route_bus";
		importObject.obj = data;
		// insert object
		imports.insertObject(importObject);
		assertTrue(importObject.id > 0);

		// get new object
		ImportObject checkObject = imports.getObject(importObject.id);
		assertEquals(importObject.id, checkObject.id);
		assertEquals(importObject.city_key, checkObject.city_key);
		assertEquals(importObject.route_number, checkObject.route_number);
		assertNotNull(importObject.obj);

		// close connection
		DBConnectionFactory.closeConnection(c);
	}

	/**
	 * Проверим функцию вставки импорт. объекта
	 * 
	 * @throws Exception
	 */
	@Test(expected = RepositoryException.class)
	public void insertFaildObjectTest() throws RepositoryException {

		System.out.println("insertRouteTest()...");

		// create repositories
		Connection c = DBConnectionFactory.getConnection();
		IimportRepository imports = new ImportRepository(c, false, false);

		// create importObject
		ImportObject importObject = new ImportObject();
		importObject.city_key = "kyiv";
		importObject.route_number = "test1_number1111";
		importObject.route_type = "c_route_bus";
		importObject.obj = null;
		// insert object
		imports.insertObject(importObject);
		assertTrue(importObject.id > 0);

		// close connection
		DBConnectionFactory.closeConnection(c);
	}

	/**
	 * Проверим функцию удаления импорт. объекта. Для начала вставим объект,
	 * затем удалим и проверим был ли он удален
	 * 
	 * @throws Exception
	 */
	@Test()
	public void removeRouteTest() throws RepositoryException {

		System.out.println("insertRouteTest()...");

		// load data
		String data = FileManager.getFileData(FileManager.getTestResourcePath()
				+ "route_import_insert.dat");

		// create repositories
		Connection c = DBConnectionFactory.getConnection();
		IimportRepository imports = new ImportRepository(c, false, false);

		// create importObject
		ImportObject importObject = new ImportObject();
		importObject.city_key = "kyiv";
		importObject.route_number = "test1_number1111";
		importObject.route_type = "c_route_bus";
		importObject.obj = data;

		// insert and object
		imports.insertObject(importObject);
		assertTrue(importObject.id > 0);
		imports.removeObject(importObject.id);

		// check
		ImportObject checkObject = imports.getObject(importObject.id);
		assertNull(checkObject);

		// close connection
		DBConnectionFactory.closeConnection(c);
	}
}