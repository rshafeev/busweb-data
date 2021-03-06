package com.pgis.bus.data.repositories.model.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pgis.bus.data.IConnectionManager;
import com.pgis.bus.data.exp.RepositoryException;
import com.pgis.bus.data.orm.type.LangEnum;
import com.pgis.bus.data.repositories.model.ICitiesModelRepository;
import com.pgis.bus.net.models.city.CityModel;
import com.pgis.bus.net.models.geom.PointModel;
import com.pgis.bus.net.models.route.RouteTypeModel;

public class CitiesModelRepository extends ModelRepository implements ICitiesModelRepository {
	private static final Logger log = LoggerFactory.getLogger(CitiesModelRepository.class);

	public CitiesModelRepository(Locale locale, IConnectionManager connManager) {
		super(locale, connManager);
	}

	public CitiesModelRepository(LangEnum langID, IConnectionManager connManager) {
		super(langID, connManager);
	}

	public CitiesModelRepository(IConnectionManager connManager) {
		super(connManager);
	}

	@Override
	public Collection<CityModel> getAll() throws RepositoryException {

		Collection<CityModel> cities = null;
		try {
			Connection c = super.getConnection();
			String query = "select bus.cities.id as id, key, lat, lon, scale, value as name from bus.cities "
					+ "join bus.string_values on string_values.key_id = name_key "
					+ "where is_show=true and lang_id = bus.lang_enum(?);";
			PreparedStatement ps = c.prepareStatement(query);
			ps.setString(1, langID);
			ResultSet rs = ps.executeQuery();

			cities = new ArrayList<CityModel>();
			while (rs.next()) {
				int id = rs.getInt("id");
				double lat = rs.getDouble("lat");
				double lon = rs.getDouble("lon");
				String cityKey = rs.getString("key");
				int scale = rs.getInt("scale");
				String name = rs.getString("name");
				CityModel city = new CityModel();
				city.setId(id);
				city.setKey(cityKey);
				city.setLocation(new PointModel(lat, lon));
				city.setScale(scale);
				city.setName(name);
				cities.add(city);
			}
		} catch (SQLException e) {
			cities = null;
			super.handeThrowble(e);
		}
		return cities;
	}

	@Override
	public CityModel get(int id) throws RepositoryException {
		try {
			Connection c = super.getConnection();
			String query = "select key, lat, lon, scale, value as name from bus.cities "
					+ "join bus.string_values on string_values.key_id = name_key "
					+ "where bus.cities.id = ? and is_show=true and lang_id = bus.lang_enum(?);";
			PreparedStatement ps = c.prepareStatement(query);
			ps.setInt(1, id);
			ps.setString(2, langID);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				double lat = rs.getDouble("lat");
				double lon = rs.getDouble("lon");
				String cityKey = rs.getString("key");
				int scale = rs.getInt("scale");
				String name = rs.getString("name");
				CityModel city = new CityModel();
				city.setId(id);
				city.setKey(cityKey);
				city.setLocation(new PointModel(lat, lon));
				city.setScale(scale);
				city.setName(name);
				return city;
			}
		} catch (SQLException e) {
			super.handeThrowble(e);
		}
		return null;
	}

	@Override
	public CityModel getByKey(String key) throws RepositoryException {

		try {
			Connection c = super.getConnection();
			String query = "select bus.cities.id as id, lat, lon, scale, value as name from bus.cities "
					+ "join bus.string_values on string_values.key_id = name_key "
					+ "where key = ? and is_show=true and lang_id = bus.lang_enum(?);";

			PreparedStatement ps = c.prepareStatement(query);
			ps.setString(1, key);
			ps.setString(2, langID);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				int id = rs.getInt("id");
				double lat = rs.getDouble("lat");
				double lon = rs.getDouble("lon");
				int scale = rs.getInt("scale");
				String name = rs.getString("name");
				CityModel city = new CityModel();
				city.setId(id);
				city.setKey(key);
				city.setLocation(new PointModel(lat, lon));
				city.setScale(scale);
				city.setName(name);
				return city;
			}
		} catch (SQLException e) {
			super.handeThrowble(e);
		}
		return null;
	}

	@Override
	public Collection<RouteTypeModel> getRouteTypesByCity(int cityID) throws RepositoryException {

		Collection<RouteTypeModel> types = new ArrayList<RouteTypeModel>();
		try {
			Connection c = super.getConnection();
			String query = "SELECT r.route_type_id from ( "
					+ "SELECT DISTINCT route_type_id FROM bus.routes where city_id = ?) as r "
					+ "JOIN bus.route_types ON bus.route_types.id = r.route_type_id " + "WHERE visible = BIT '1';";

			PreparedStatement ps = c.prepareStatement(query);
			ps.setInt(1, cityID);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				types.add(new RouteTypeModel(rs.getString(1)));
			}
		} catch (SQLException e) {
			super.handeThrowble(e);
		}
		return types;
	}

}
