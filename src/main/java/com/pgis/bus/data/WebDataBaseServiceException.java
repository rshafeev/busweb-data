package com.pgis.bus.data;

public class WebDataBaseServiceException extends Exception {
	public enum err_enum {
		c_connect_to_db_err(1);

		private final int value;

		private err_enum(int bar) {
			this.value = bar;
		}

		public int getValue() {
			return value;
		}

		public String getMessage() {
			switch (this) {
			case c_connect_to_db_err:
				return "DB connection problem";
			}
			return "unknown error";
		}
	}

	WebDataBaseServiceException(err_enum err) {
		super(err.getMessage());

	}
}
