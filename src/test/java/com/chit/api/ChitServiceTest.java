//package com.chit.api;
//
//import com.chit.api.dao.ChitRepo;
//import com.chit.api.dao.model.ChitDBModel;
//import com.chit.api.dao.model.Slot;
//import com.chit.api.globalexceptions.BadRequestException;
//import com.chit.api.globalexceptions.ResourceExistsException;
//import com.chit.api.request.model.ChitRequest;
//import com.chit.api.sequence.SequenceService;
//import com.chit.api.service.ChitService;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.MockitoAnnotations;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import static org.mockito.ArgumentMatchers.any;
//
//
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//import java.util.stream.IntStream;
//
//import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@SpringBootTest(classes = ChitService.class)
//@ExtendWith(MockitoExtension.class)
//public class ChitServiceTest {
//
//    @Autowired
//    private ChitService chitService;
//
//    @MockBean
//    private SequenceService sequenceService;
//
//    @MockBean
//    private ChitRepo chitRepo;
//
//    private ChitRequest chitRequest;
//
//    @Test
//    public void testGetAllChits() {
//       List<ChitDBModel> chitDBModels = new ArrayList<>();
//        ChitDBModel chitDBModel1 = new ChitDBModel();
//        chitDBModel1.setId(1L);
//        chitDBModel1.setChitname("Chit 1");
//        chitDBModels.add(chitDBModel1);
//
//        ChitDBModel chitDBModel2 = new ChitDBModel();
//        chitDBModel2.setId(2L);
//        chitDBModel2.setChitname("Chit 2");
//        chitDBModels.add(chitDBModel2);
//        when(chitRepo.findAll()).thenReturn(chitDBModels);
//
//        List<ChitDBModel> result = chitService.getAllChits();
//
//        assertEquals(2, result.size());
//        assertEquals("Chit 1", result.get(0).getChitname());
//        assertEquals("Chit 2", result.get(1).getChitname());
//    }
//
//    @Test
//    public void testAddChit_BadRequestException() {
//        ChitRequest chitRequest = new ChitRequest();
//        chitRequest.setChitname(null);
//        assertThrows(BadRequestException.class, () -> chitService.addChit(chitRequest));
//    }
//
//
//    @Test
//    public void testCount() {
//        when(chitRepo.count()).thenReturn(10L);
//
//        long count = chitService.count();
//        assertEquals(10, count);
//    }
//
//    @Test
//    public void testGetChitByName_BadRequestException() {
//        String name = null;
//        assertThrows(BadRequestException.class, () -> chitService.getChitByName(name));
//    }
//
//    @Test
//    public void testGetChitByName_ChitExists() {
//        String name = "existingChitName";
//        ChitDBModel existingChit = new ChitDBModel();
//        existingChit.setChitname(name);
//
//        when(chitRepo.findByChitname(name)).thenReturn(existingChit);
//
//        assertThrows(BadRequestException.class, () -> chitService.getChitByName(name));
//    }
//
//
//
//    @Test
//    public void testDeleteChit() {
//        chitService.deleteChit();
//    }
//
//    @Test
//    public void testGetChitByName_NullName() {
//        String nullName = null;
//        assertThrows(BadRequestException.class, () -> chitService.getChitByName(nullName));
//    }
//
//
//
//    @Test
//    public void testDelete_ResourceExistsException() {
//        String chitname = "Test Chit";
//        when(chitRepo.findByChitname(chitname)).thenReturn(null);
//
//        assertThrows(ResourceExistsException.class, () -> chitService.delete(chitname));
//    }
//
//    @Test
//    void shouldThrowBadRequestExceptionWhenChitnameIsNull() {
//        ChitRequest chitRequest = new ChitRequest();
//        chitRequest.setTenure("12");
//        chitRequest.setInstallment("5000");
//        chitRequest.setAmount("60000");
//        chitRequest.setStartDate(String.valueOf(LocalDate.now()));
//        chitRequest.setEndDate(String.valueOf(LocalDate.now().plusMonths(12)));
//
//        assertThrows(BadRequestException.class, () -> chitService.addChit(chitRequest));
//    }
//
//    @Test
//    void shouldSaveChitDBModelWithCorrectDetails() {
//        MockitoAnnotations.openMocks(this);
//
//        ChitRequest chitRequest = new ChitRequest();
//        chitRequest.setChitname("testChit");
//        chitRequest.setTenure("10");
//        chitRequest.setInstallment("1000");
//        chitRequest.setAmount("10000");
//        chitRequest.setStartDate(String.valueOf(LocalDate.now()));
//        chitRequest.setEndDate(String.valueOf(LocalDate.now().plusYears(1)));
//
//        ChitDBModel expectedChitDBModel = new ChitDBModel();
//        expectedChitDBModel.setId(1L);
//        expectedChitDBModel.setChitname("testChit");
//        expectedChitDBModel.setAmount("10000");
//        expectedChitDBModel.setTenure("10");
//        expectedChitDBModel.setInstallment("1000");
//        expectedChitDBModel.setStartDate(String.valueOf(LocalDate.now()));
//        expectedChitDBModel.setEndDate(String.valueOf(LocalDate.now().plusYears(1)));
//        expectedChitDBModel.setChitstatus("CHIT_CREATED");
//        expectedChitDBModel.setBalanceAmount("10000");
//
//        int maxSlots = 10;
//        List<Slot> expectedSlotList = IntStream.range(0, maxSlots)
//                .mapToObj(i -> new Slot(i, "1000"))
//                .collect(Collectors.toCollection(ArrayList::new));
//        expectedChitDBModel.setSlots(expectedSlotList);
//
//        when(sequenceService.generateSequence(ChitDBModel.CHIT_SEQUENCE)).thenReturn(1L);
//        when(chitRepo.save(any(ChitDBModel.class))).thenReturn(expectedChitDBModel);
//        when(chitRepo.findAll()).thenReturn(List.of(expectedChitDBModel));
//
//        long savedChitId = chitService.addChit(chitRequest);
//
//        assertThat(savedChitId).isEqualTo(1L);
//        verify(sequenceService).generateSequence(ChitDBModel.CHIT_SEQUENCE);
//        verify(chitRepo).save(any(ChitDBModel.class));
//
//        List<ChitDBModel> allChits = chitService.getAllChits();
//        System.out.println("All Chits: " + allChits);
//
//        assertThat(allChits).isNotNull();
//        assertThat(allChits.get(0)).usingRecursiveComparison().ignoringFields("id").isEqualTo(expectedChitDBModel);
//    }
//
//
//
//}
