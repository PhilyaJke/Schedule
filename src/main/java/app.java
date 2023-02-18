import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.messages.*;
import com.vk.api.sdk.queries.messages.MessagesGetLongPollHistoryQuery;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class app {
    public static void main(String[] args) throws ClientException, ApiException, InterruptedException {

        Random random = new Random();
        TransportClient transportClient = new HttpTransportClient();
        VkApiClient vkApiClient = new VkApiClient(transportClient);
        GroupActor groupActor = new GroupActor(218910408, "vk1.a.cTtE0scfXVoIe57vbBcarHtepeHfIOjP3XOCuDPje5oZ84fL-OgjqK_RS9DC4ZF6x8wlrnYk-3xac5nsFfqxCe-7Nk-RJCC8fhMZvbLbi60-4kb_S0J6MTotz0VdNsPurlNlG0Yo5oFu7fNNuT04mwmMA2EnpBhhViP71LH11mwTMdwteO39TY5UwJ2aFX9FfYC4Vpn49Ex5LI-EwQCWiw");

        Integer ts = vkApiClient.messages().getLongPollServer(groupActor).execute().getTs();
        while(true){
            MessagesGetLongPollHistoryQuery messagesGetLongPollHistoryQuery = vkApiClient.messages()
                    .getLongPollHistory(groupActor).ts(ts);
            List<Message> messages = messagesGetLongPollHistoryQuery.execute().getMessages().getItems();
            if(!messages.isEmpty()){
                messages.forEach(message -> {
                    System.out.println(message.toString());
                    if(message.getText().equals("Начать")){
                        try {
                            vkApiClient.messages().send(groupActor)
                                    .message("Чтобы узнать расписание напиши сообщение в формате 'направление-год-группа'")
                                    .userId(message.getFromId()).randomId(random.nextInt(10000)).execute();
                        } catch (ApiException e) {
                            e.printStackTrace();
                        } catch (ClientException e) {
                            e.printStackTrace();
                        }
                    }else if(message.getText().matches("^([А-Я]{4}[-]{1}[0-9]{2}[-]{1}[0-9]{1})||([А-Я]{4}[-]{1}[0-9]{2}[-]{1}[0-9]{2})$")){
                        Data data = new Data();
                        try {
                            vkApiClient.messages().send(groupActor)
                                    .message(data.find(message.getText().toUpperCase(Locale.ROOT)))
                                    .userId(message.getFromId()).randomId(random.nextInt(10000)).execute();
                        } catch (ApiException | IOException e) {
                            e.printStackTrace();
                        } catch (ClientException e) {
                            e.printStackTrace();
                        }
                    }else{
                        try {
                            vkApiClient.messages().send(groupActor)
                                    .message("Неправильный формат ввода").userId(message.getFromId()).randomId(random.nextInt(10000)).execute();
                        } catch (ApiException e) {
                            e.printStackTrace();
                        } catch (ClientException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            ts = vkApiClient.messages().getLongPollServer(groupActor).execute().getTs();
            Thread.sleep(500);
        }
    }
}

class Data{
    int test;
    String result = " ";
    int indexRow;
    int indexCell;
    StringBuilder stringBuilder = new StringBuilder();

    public String find(String group) throws IOException {
        String[] str = group.split("-");
        File folder = new File("/Users/philyaborozdin/Desktop/Расписание всех факов");
        File[] files = folder.listFiles();
        for(int i = 0; i < files.length; i++){
            if(str[0].equals(files[i].getName().substring(0, files[i].getName().length()-4))) {
                if (searchGroup(group, files[i].toString(), str[1]) == 1) {
                    result = printData(files[i].getAbsolutePath(), convertYearToCourse(str[1]));
                    break;
                }
            }
        }
        if(result.equals(" ")){
            result = "не найдено";
        }
        return result;
    }

    public String convertYearToCourse(String course){
        String sheet = "";
        switch (course){
            case "22" -> sheet = "1 курс";
            case "21" -> sheet = "2 курс";
            case "20" -> sheet = "3 курс";
            case "19" -> sheet = "4 курс";
        }
        return sheet;
    }

    public int searchGroup(String group, String file, String course) throws IOException {
        FileInputStream inputStream = new FileInputStream(file);
        Workbook workbook = new HSSFWorkbook(inputStream);
        String sheet = convertYearToCourse(course);
        for(Row row: workbook.getSheet(sheet)){
            for (Cell cell: row){
                Cell r = cell;
                if(getCellText(cell).equals(group)){
                    indexRow = r.getRowIndex();
                    indexCell = r.getColumnIndex();
                    test = 1;
                    break;
                }
            }
        }
        return test;
    }

    public String getCellText(Cell cell){
        String res = "";
        switch (cell.getCellType()){
            case Cell.CELL_TYPE_STRING:
                res = cell.getRichStringCellValue().getString();
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if(DateUtil.isCellDateFormatted(cell)){
                    res = cell.getDateCellValue().toString();
                }else{
                    res = Double.toString(cell.getNumericCellValue());
                }
                break;
            case Cell.CELL_TYPE_BOOLEAN:
                res = Boolean.toString(cell.getBooleanCellValue());
                break;
            case Cell.CELL_TYPE_FORMULA:
                res = cell.getCellFormula();
                break;
            default:
                break;
        }
        return res;
    }

    public String printData(String file, String course) throws IOException {
        FileInputStream inputStream = new FileInputStream(file);
        Workbook workbook = new HSSFWorkbook(inputStream);
        int newDay = 0;
        int indexPredmet = indexRow + 1;
        for(int i = 0; i < 7; i++){
            for(int j = 0; j < 14; j+=2){
                String predmet = getCellText(workbook.getSheet(course)
                        .getRow(indexRow + j + newDay).getCell(indexCell)) + "\n";
                stringBuilder.append(predmet);
            }
            newDay+=12;
        }
        return stringBuilder.toString();
    }
}