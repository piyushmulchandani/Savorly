package com.application.savorly.config;

import com.application.savorly.SavorlyApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest(classes = {SavorlyApplication.class})
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:/application-test.properties")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class JwtAuthConverterTest {

    @InjectMocks
    private JwtAuthConverter jwtAuthConverter;

    @Mock
    private Jwt jwt;

    @BeforeEach
    void setUp() throws Exception {
        setPrivateField(jwtAuthConverter, "principleAttribute", "preferred_username");
        setPrivateField(jwtAuthConverter, "resourceId", "savorly-api");
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void convert_ShouldReturnJwtAuthenticationToken_WithRolesAndClaims() {
        when(jwt.getClaim(JwtClaimNames.SUB)).thenReturn("user123");
        when(jwt.getClaim("preferred_username")).thenReturn("testUser");
        when(jwt.getClaim("resource_access")).thenReturn(Map.of(
                "savorly-api", Map.of("roles", List.of("ADMIN", "USER"))
        ));

        JwtAuthenticationToken authToken = (JwtAuthenticationToken) jwtAuthConverter.convert(jwt);

        assertThat(authToken).isNotNull();
        assertThat(authToken.getName()).isEqualTo("testUser");

        Collection<GrantedAuthority> authorities = authToken.getAuthorities();
        assertThat(authorities)
                .extracting("authority")
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER");
    }

    @Test
    void convert_ShouldReturnJwtAuthenticationToken_WithNoRoles_WhenResourceAccessIsMissing() {
        when(jwt.getClaim(JwtClaimNames.SUB)).thenReturn("user123");
        when(jwt.getClaim("preferred_username")).thenReturn("testUser");
        when(jwt.getClaim("resource_access")).thenReturn(null);

        JwtAuthenticationToken authToken = (JwtAuthenticationToken) jwtAuthConverter.convert(jwt);

        assertThat(authToken).isNotNull();
        assertThat(authToken.getName()).isEqualTo("testUser");
        assertThat(authToken.getAuthorities()).isEmpty();
    }

    @Test
    void convert_ShouldFallbackToSubClaim_WhenPrincipleAttributeIsNull() throws Exception {
        setPrivateField(jwtAuthConverter, "principleAttribute", null);

        when(jwt.getClaim(JwtClaimNames.SUB)).thenReturn("user456");

        JwtAuthenticationToken authToken = (JwtAuthenticationToken) jwtAuthConverter.convert(jwt);

        assertThat(authToken).isNotNull();
        assertThat(authToken.getName()).isEqualTo("user456");
    }
}