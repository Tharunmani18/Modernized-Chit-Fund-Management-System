package com.chit.api;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chit.api.controller.ChitController;
import com.chit.api.enums.ChitEnum;
import com.chit.api.globalexceptions.ResourceNotFoundException;
import com.chit.api.request.model.ChitRequest;
import com.chit.api.response.model.ChitResponse;
import com.chit.api.response.model.CountResponse;
import com.chit.api.service.ChitService;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(classes = ChitController.class)
@ExtendWith(MockitoExtension.class)
class ChitControllerTest {

    @Autowired
    private ChitController chitController;

    @MockBean
    private ChitService chitService;

    @Test
    @Order(1)
    void addChitTest() {
        ChitRequest chitRequest = new ChitRequest();
        chitRequest.setChitname("NewName");
        when(chitService.addChit(chitRequest)).thenReturn(1L);
        ResponseEntity<?> response = chitController.addChit(chitRequest);
        ChitResponse chitResponse = (ChitResponse) response.getBody();
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(ChitEnum.CHIT_CREATED.getEnumChitConstant().toString(),
            chitResponse.getMessage());
        assertEquals(1L, chitResponse.getId());
    }

    // @Test
//    @Order(2)
//    void getChitsTest() {
//        List<Slot> slots = List.of(
//                new Slot(1, "10000"),
//                new Slot(2, "10000")
//        );
//        ChitDBModel chitDBModel1 = new ChitDBModel();
//        chitDBModel1.setChitname("TestChit");
//        chitDBModel1.setAmount("100");
//        chitDBModel1.setSlots(slots);
//
//        ChitDBModel chitDBModel2 = new ChitDBModel();
//        chitDBModel1.setChitname("TestChit1");
//        chitDBModel1.setAmount("1000");
//        chitDBModel1.setSlots(slots);
//
//        List<ChitDBModel> chitList = List.of(chitDBModel1, chitDBModel2);
//
//        when(chitService.getAllChits()).thenReturn(chitList);
//
//        ResponseEntity<?> responseEntity = chitController.getChits();
//
//        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
//        assertEquals(chitList, responseEntity.getBody());
//    }

    @Test
    @Order(3)
    void getChitCountTest() {
        long chitCount = 5;
        when(chitService.count()).thenReturn(chitCount);

        ResponseEntity<?> responseEntity = chitController.getCount();

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        CountResponse countResponse = (CountResponse) responseEntity.getBody();
        assertEquals(chitCount, countResponse.getCount());

    }

    @Test
    @Order(4)
    void deleteAllChitsTest() {
        doNothing().when(chitService).deleteChit();

        ResponseEntity<ChitResponse> responseEntity = (ResponseEntity<ChitResponse>) chitController.deleteChits();

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody().getMessage()).isEqualTo(
            ChitEnum.ALL_CHITS_DELETED.getEnumChitConstant());

        verify(chitService).deleteChit();
    }

    @Test
    @Order(5)
    void deleteChitByNameTest() {
        ChitRequest chitRequest = new ChitRequest();
        chitRequest.setChitname("Test Chit");

        ResponseEntity<?> responseEntity = chitController.deleteUser(chitRequest);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isInstanceOf(ChitResponse.class);
        ChitResponse chitResponse = (ChitResponse) responseEntity.getBody();
        assertThat(chitResponse.getMessage()).isEqualTo(
            ChitEnum.CHIT_DELETED.getEnumChitConstant());
    }

    @Test
    public void testDeleteChits_ResourceNotFoundException() {
        doThrow(new ResourceNotFoundException(ChitEnum.CHIT_EMPTY.getEnumChitConstant()))
            .when(chitService).deleteChit();

        ResponseEntity<ChitResponse> responseEntity = (ResponseEntity<ChitResponse>) chitController.deleteChits();

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        ChitResponse chitResponse = responseEntity.getBody();
        assertNotNull(chitResponse);
        assertEquals(ChitEnum.CHIT_EMPTY.getEnumChitConstant(), chitResponse.getMessage());
    }
}
