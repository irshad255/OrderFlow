
package com.orderflow;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

class OrderflowApplicationTests {

	@Test
	void mainShouldInvokeSpringApplication() {
		try (MockedStatic<SpringApplication> mocked =
					 mockStatic(SpringApplication.class)) {

			OrderflowApplication.main(new String[0]);

			mocked.verify(() ->
					SpringApplication.run(
							OrderflowApplication.class,
							new String[0]
					)
			);
		}
	}
}
/*- SpringApplication.class — Mockito ko batata hai ki kis class ke static methods mock karne hain.
- mockStatic(...) — us class ke static methods ke liye mock control establish karta hai.
- MockedStatic<SpringApplication> mocked — us mock ko control aur verify karne ke liye reference hai.
*/
