package com.pgis.bus.data.repositories.orm;

import java.sql.SQLException;

import com.pgis.bus.data.exp.RepositoryException;
import com.pgis.bus.data.orm.User;
import com.pgis.bus.data.orm.type.AuthenticateResult;
import com.pgis.bus.data.repositories.IRepository;

public interface IUsersRepository extends IRepository {
	User get(int id) throws RepositoryException;

	AuthenticateResult authenticate(String userRole, String userName, String userPassword) throws RepositoryException;

}
