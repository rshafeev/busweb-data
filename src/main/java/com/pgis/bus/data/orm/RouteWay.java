package com.pgis.bus.data.orm;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import com.pgis.bus.data.IConnectionManager;
import com.pgis.bus.data.orm.type.LangEnum;
import com.pgis.bus.data.orm.type.LineStringEx;
import com.pgis.bus.data.repositories.orm.impl.RoutesRepository;
import com.pgis.bus.data.repositories.orm.impl.ScheduleRepository;
import com.pgis.bus.net.models.route.RouteWayModel;

public class RouteWay extends ORMObject implements Cloneable {
	private Integer id;
	private int routeID;
	private boolean direct;

	private Collection<RouteRelation> route_relations;
	private Schedule schedule;

	public RouteWay() {
		super();
	}

	public RouteWay(IConnectionManager connManager) {
		super(connManager);
	}

	public Schedule getSchedule() throws SQLException {
		if (schedule == null && super.connManager != null) {
			ScheduleRepository rep = null;
			try {
				rep = new ScheduleRepository(super.connManager);
				this.schedule = rep.getByRouteWay(this.id);
			} finally {

				if (rep != null)
					rep.dispose();
			}
		}
		return schedule;
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
		if (schedule != null) {
			schedule.setRouteWayId(id);
		}
		if (route_relations != null) {
			for (RouteRelation r : route_relations) {
				r.setRouteWayID(id);
			}

		}
	}

	public int getRouteID() {
		return routeID;
	}

	public void setRouteID(int routeID) {
		this.routeID = routeID;
	}

	public boolean isDirect() {
		return direct;
	}

	public void setDirect(boolean direct) {
		this.direct = direct;
	}

	public Collection<RouteRelation> getRouteRelations() throws SQLException {
		if (route_relations == null && super.connManager != null) {
			RoutesRepository rep = null;
			try {
				rep = new RoutesRepository(super.connManager);
				this.route_relations = rep.getRouteWayRelations(this.id);
			} finally {
				if (rep != null)
					rep.dispose();
			}

		}
		return route_relations;

	}

	public void setRouteRelations(Collection<RouteRelation> route_relations) {
		this.route_relations = route_relations;
	}

	public static RouteWay createReverseByDirect(RouteWay directRouteWay) throws SQLException,
			CloneNotSupportedException {
		directRouteWay = directRouteWay.clone();
		RouteWay r = new RouteWay();
		r.setDirect(false);
		r.setId(-1);
		r.setRouteID(directRouteWay.getRouteID());
		r.setSchedule(directRouteWay.getSchedule());

		ArrayList<RouteRelation> relations = new ArrayList<RouteRelation>();
		RouteRelation[] arr = directRouteWay.getRouteRelations().toArray(
				new RouteRelation[directRouteWay.getRouteRelations().size()]);
		for (int i = 0; i < arr.length; i++) {
			RouteRelation newRelation = new RouteRelation();
			newRelation.setPositionIndex(i);
			newRelation.setId(-i);
			newRelation.setRouteWayID(arr[arr.length - i - 1].getRouteWayID());
			if (i == 0) {
				newRelation.setStationAId(0);
			} else {
				newRelation.setStationAId(arr[arr.length - i].getStationBId());
				newRelation.setGeom(arr[arr.length - i].getGeom());
				newRelation.setDistance(arr[arr.length - i].getDistance());
				newRelation.setMoveTime(arr[arr.length - i].getMoveTime());
			}
			newRelation.setStationBId(arr[arr.length - i - 1].getStationBId());
			newRelation.setStationB(arr[arr.length - i - 1].getStationB());
			relations.add(newRelation);
		}
		r.setRouteRelations(relations);
		return r;
	}

	public void updateIDs() throws SQLException {
		RouteRelation[] arr = this.getRouteRelations().toArray(new RouteRelation[this.getRouteRelations().size()]);
		for (int i = 0; i < arr.length; i++) {
			if (i == 0) {
				arr[0].setStationAId(0);

			} else {
				arr[i].setStationAId(arr[i - 1].getStationBId());
			}
			Station stB = arr[i].getStationB();
			if (stB != null) {
				arr[i].setStationBId(stB.getId());
			}
			arr[i].setPositionIndex(i);
		}
	}

	@Override
	public String toString() {
		return "RouteWay [id=" + id + ", routeID=" + routeID + ", direct=" + direct + ", route_relations="
				+ route_relations + ", schedule=" + schedule + "]";
	}

	RouteWayModel createModel(RouteWay way, LangEnum langID) throws SQLException {
		RouteWayModel model = new RouteWayModel();
		model.setSchedule(way.getSchedule().toModel());
		model.setId(way.getId());
		model.setRelations(RouteRelation.createModels(way.getRouteRelations(), langID));
		return model;
	}

	public RouteWayModel toModel(LangEnum langID) throws Exception {
		return createModel(this, langID);
	}

	public Collection<RouteRelation> makeReverseRelations() throws SQLException {
		if (this.route_relations == null)
			return null;
		Collection<RouteRelation> reverse = new ArrayList<RouteRelation>();
		RouteRelation[] relations = this.route_relations.toArray(new RouteRelation[this.route_relations.size()]);
		for (int i = 0; i < relations.length; i++) {
			RouteRelation r = relations[relations.length - 1 - i];
			RouteRelation newRelation = new RouteRelation();
			newRelation.setStationBId(r.getStationBId());
			if (i == 0) {
				newRelation.setGeom(null);
				newRelation.setStationA(null);
				newRelation.setStationAId(-1);
			} else {
				RouteRelation prevRelation = relations[relations.length - i];
				newRelation.setGeom(prevRelation.getGeom().reverse());
				newRelation.setMoveTime(prevRelation.getMoveTime());
				newRelation.setDistance(prevRelation.getDistance());

			}
			newRelation.setStationB(r.getStationB());
			newRelation.setPositionIndex(i);
			newRelation.setRouteWayID(this.id);
			reverse.add(newRelation);

		}
		return reverse;
	}

	@Override
	public RouteWay clone() throws CloneNotSupportedException {
		Collection<RouteRelation> wayRelations = new ArrayList<RouteRelation>();
		if (this.route_relations != null) {
			for (RouteRelation r : this.route_relations) {
				wayRelations.add(r.clone());
			}
		}
		RouteWay way = (RouteWay) super.clone();
		way.direct = this.direct;
		way.id = this.id;
		way.routeID = this.routeID;
		if (this.schedule != null) {
			way.schedule = this.schedule.clone();
		}
		way.setRouteRelations(wayRelations);
		return way;
	}
}
