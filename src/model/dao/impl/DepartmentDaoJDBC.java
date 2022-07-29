package model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import db.DB;
import db.DbException;
import model.dao.DepartmentDao;
import model.entities.Department;

public class DepartmentDaoJDBC implements DepartmentDao {

	Connection conn = null;

	public DepartmentDaoJDBC(Connection conn) {
		this.conn = conn;
	}

	@Override
	public void insert(Department obj) {
		PreparedStatement stm = null;

		try {

			stm = conn.prepareStatement("INSERT INTO department (Name) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
			stm.setString(1, obj.getName());

			int rowsAffected = stm.executeUpdate();

			if (rowsAffected > 0) {
				ResultSet rs = stm.getGeneratedKeys();
				if (rs.next()) {
					int id = rs.getInt(1);
					obj.setId(id);
				}
				DB.closeResultSet(rs);
			} else {
				throw new DbException("Unexpected error! No rows affected");
			}
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeStatement(stm);
		}
	}

	@Override
	public void update(Department obj) {
		PreparedStatement stm = null;
		
		try {
			stm = conn.prepareStatement("UPDATE department SET Name = ? WHERE Id = ?");
			stm.setString(1, obj.getName());
			stm.setInt(2, obj.getId());
			stm.executeUpdate();
			
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeStatement(stm);
		}
	}

	@Override
	public void deletById(Integer id) {
		PreparedStatement stm = null;
		
		try {
			stm = conn.prepareStatement("DELETE FROM department WHERE Id = ?");
			stm.setInt(1, id);
			stm.executeUpdate();
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeStatement(stm);
		}
	}

	@Override
	public Department findById(Integer id) {

		PreparedStatement stm = null;
		ResultSet resultSet = null;

		try {
			stm = conn.prepareStatement("SELECT * FROM department WHERE Id = ?");
			stm.setInt(1, id);
			resultSet = stm.executeQuery();

			if (resultSet.next()) {
				Department dep = getDepartment(resultSet);

				return dep;
			}

			return null;

		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeStatement(stm);
			DB.closeResultSet(resultSet);
		}
	}

	@Override
	public List<Department> findAll() {
		PreparedStatement stm = null;
		ResultSet resultSet = null;
		List<Department> list = new ArrayList<Department>();

		try {
			stm = conn.prepareStatement("SELECT * FROM department");
			resultSet = stm.executeQuery();
			while (resultSet.next()) {
				Department dep = getDepartment(resultSet);
				list.add(dep);
			}
			return list;
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeStatement(stm);
			DB.closeResultSet(resultSet);
		}

	}

	private Department getDepartment(ResultSet resultSet) throws SQLException {
		Department dep = new Department();
		dep.setId(resultSet.getInt("Id"));
		dep.setName(resultSet.getString("Name"));
		return dep;
	}

}
