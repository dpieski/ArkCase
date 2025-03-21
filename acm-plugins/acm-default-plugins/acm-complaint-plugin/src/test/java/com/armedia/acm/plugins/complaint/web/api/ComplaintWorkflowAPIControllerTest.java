package com.armedia.acm.plugins.complaint.web.api;

/*-
 * #%L
 * ACM Default Plugin: Complaints
 * %%
 * Copyright (C) 2014 - 2018 ArkCase LLC
 * %%
 * This file is part of the ArkCase software. 
 * 
 * If the software was purchased under a paid ArkCase license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * ArkCase is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * ArkCase is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with ArkCase. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.armedia.acm.plugins.addressable.model.PostalAddress;
import com.armedia.acm.plugins.complaint.model.Complaint;
import com.armedia.acm.plugins.complaint.service.ComplaintEventPublisher;
import com.armedia.acm.plugins.person.model.Person;
import com.armedia.acm.plugins.person.model.PersonAssociation;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

import java.util.Arrays;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/spring/spring-web-acm-web.xml",
        "classpath:/spring/spring-library-complaint-plugin-unit-test.xml"
})
public class ComplaintWorkflowAPIControllerTest extends EasyMockSupport
{
    private MockMvc mockMvc;
    private ComplaintEventPublisher mockEventPublisher;
    private Authentication mockAuthentication;

    @Autowired
    private ExceptionHandlerExceptionResolver exceptionResolver;

    private ComplaintWorkflowAPIController unit;

    private Logger log = LogManager.getLogger(getClass());

    @Before
    public void setUp() throws Exception
    {
        mockEventPublisher = createMock(ComplaintEventPublisher.class);
        mockAuthentication = createMock(Authentication.class);

        unit = new ComplaintWorkflowAPIController();
        unit.setEventPublisher(mockEventPublisher);

        mockMvc = MockMvcBuilders.standaloneSetup(unit).setHandlerExceptionResolvers(exceptionResolver).build();

    }

    @Test
    public void startApprovalWorkflow() throws Exception
    {

        Complaint complaint = new Complaint();
        complaint.setComplaintId(500L);
        complaint.setComplaintType("complaintType");

        Person person = new Person();
        person.setFamilyName("Jones");
        person.setGivenName("David");
        PostalAddress address = new PostalAddress();
        address.setCity("Falls Church");
        address.setState("VA");
        address.setStreetAddress("8221 Old Courthouse Road");
        person.getAddresses().add(address);

        PersonAssociation personAssoc = new PersonAssociation();
        personAssoc.setPerson(person);
        personAssoc.setPersonDescription("sample Description");
        personAssoc.setPersonType("Originator");

        complaint.setOriginator(personAssoc);

        complaint.setApprovers(Arrays.asList("user1", "user2"));

        ObjectMapper objectMapper = new ObjectMapper();
        String in = objectMapper.writeValueAsString(complaint);

        log.debug("Input JSON: " + in);

        Capture<Complaint> capturedComplaint = EasyMock.newCapture();

        mockEventPublisher.publishComplaintWorkflowEvent(
                capture(capturedComplaint),
                eq(mockAuthentication),
                eq("acm_ip_address"),
                eq(true));

        // MVC test classes must call getName() somehow
        expect(mockAuthentication.getName()).andReturn("user");

        replayAll();

        MockHttpSession mockSession = new MockHttpSession();
        mockSession.setAttribute("acm_ip_address", "acm_ip_address");
        mockMvc.perform(
                post("/api/latest/plugin/complaint/workflow")
                        .accept(MediaType.parseMediaType("application/json;charset=UTF-8"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .session(mockSession)
                        .principal(mockAuthentication)
                        .content(in))
                .andReturn();

        verifyAll();

        Complaint handled = capturedComplaint.getValue();

        assertEquals(complaint.getComplaintNumber(), handled.getComplaintNumber());
    }

    @Test
    public void startApprovalWorkflow_complaintNotSavedYet() throws Exception
    {

        Complaint complaint = new Complaint();
        complaint.setComplaintId(0L);

        ObjectMapper objectMapper = new ObjectMapper();
        String in = objectMapper.writeValueAsString(complaint);

        log.debug("Input JSON: " + in);

        Capture<Complaint> capturedComplaint = EasyMock.newCapture();

        mockEventPublisher.publishComplaintWorkflowEvent(
                capture(capturedComplaint),
                eq(mockAuthentication),
                eq("acm_ip_address"),
                eq(false));

        // MVC test classes must call getName() somehow
        expect(mockAuthentication.getName()).andReturn("user");

        replayAll();

        MockHttpSession mockSession = new MockHttpSession();
        mockSession.setAttribute("acm_ip_address", "acm_ip_address");
        mockMvc.perform(
                post("/api/latest/plugin/complaint/workflow")
                        .accept(MediaType.parseMediaType("application/json;charset=UTF-8"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .session(mockSession)
                        .principal(mockAuthentication)
                        .content(in))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN));

        verifyAll();

        Complaint handled = capturedComplaint.getValue();

        assertEquals(complaint.getComplaintNumber(), handled.getComplaintNumber());
    }
}
