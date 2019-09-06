package com.example.karthikm.merlinwmscipher_vip_rdc.interfaces;

import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.WMSCoreMessage;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;


public interface ApiInterface {
    @POST("Login/Checktest")
    Call <String>  Checktest(@Body WMSCoreMessage oRequest);
    @POST("Login/UserLogin")
    Call<String>  UserLogin(@Body WMSCoreMessage oRequest);
    @POST("Inbound/GetInboundByID")
    Call <String>  GetInboundByUserId(@Body WMSCoreMessage oRequest);
    @POST("Inbound/GetInboundListForPutAway")
    Call <String>  GetInboundListForPutAway(@Body WMSCoreMessage oRequest);
    @POST("Inbound/ValidateLocation")
    Call <String>  ValidateLocationCode(@Body WMSCoreMessage oRequest);
    @POST("Inbound/ValidatePallet")
    Call <String>  ValidatePalletCode(@Body WMSCoreMessage oRequest);
    @POST("Inbound/ValidateRSN")
    Call <String> ValidateRSNAndReceive(@Body WMSCoreMessage oRequest);
    @POST("Inbound/PalletComplete")
    Call <String> PalletComplete(@Body WMSCoreMessage oRequest);
    @POST("Inbound/GeneratePutawaySuggestions")
    Call <String> GeneratePutawaySuggestions(@Body WMSCoreMessage oRequest);
    @POST("Inbound/BincompleteWithoutPallet")
    Call <String> BincompleteWithoutPallet(@Body WMSCoreMessage oRequest);
    @POST("Inbound/PutAwayComplete")
    Call <String> PutAwayComplete(@Body WMSCoreMessage oRequest);

    @POST("Inbound/UpdatePutAwayItem")
    Call <String> UpdatePutAwayItem(@Body WMSCoreMessage oRequest);
    @POST("Inbound/PutawayPalletComplete")
    Call <String> PutAwayPalletComplete(@Body WMSCoreMessage oRequest);
    @POST("Inbound/BinComplete")
    Call <String> Bincomplete(@Body WMSCoreMessage oRequest);
    @POST("Inbound/ValidatePalletForPutAway")
    Call <String>  ValidatePalletForPutAway(@Body WMSCoreMessage oRequest);
    @POST("Inbound/GetPutawaySuggestionsBasedOnSerialNo")
    Call <String>  GetPutawaySuggestionsBasedOnSerialNo(@Body WMSCoreMessage oRequest);
    @POST("Inbound/ValidateLocationForPutAWay")
    Call <String>  ValidateLocationForPutAWay(@Body WMSCoreMessage oRequest);
    @POST("Inbound/UpdatePutAwayItemWithoutPallet")
    Call <String>  UpdatePutAwayItemWithoutPallet(@Body WMSCoreMessage oRequest);
    @POST("Inbound/FetchPutawaySuggestions")
    Call <String> FetchPutawaySuggestions(@Body WMSCoreMessage oRequest);
    @POST("CycleCount/GetCCList")
    Call <String> GetCycleCountByUserId(@Body WMSCoreMessage oRequest);
    @POST("CycleCount/FetchLocForCC")
    Call <String> FetchLocationForCycleCount(@Body WMSCoreMessage oRequest);
    @POST("CycleCount/BlockLocForCC")
    Call <String> BlockLocationForCycleCount(@Body WMSCoreMessage oRequest);
    @POST("CycleCount/CaptureCC")
    Call <String> CaptureCycleCount(@Body WMSCoreMessage oRequest);
    @POST("CycleCount/MarkBinComplete")
    Call <String> MarkBinComplete(@Body WMSCoreMessage oRequest);
    @POST("CycleCount/ValidateLocBoxQty")
    Call <String> ValidateLocationBoxQuantity(@Body WMSCoreMessage oRequest);
    @POST("Outbound/GetPickRefNo")
    Call <String> GetPickRefNo(@Body WMSCoreMessage oRequest);
    @POST("Outbound/GetLoadCancelSheetNo")
    Call <String> GetLoadCancelSheetNo(@Body WMSCoreMessage oRequest);
    @POST("Outbound/GetLoadSheetNo")
    Call <String> GetLoadSheetNo(@Body WMSCoreMessage oRequest);
    @POST("Outbound/UpdatePickingItem")
    Call <String> UpdatePickingItem(@Body WMSCoreMessage oRequest);
    @POST("Outbound/MarkMaterialNotFound")
    Call <String> MarkMaterialNotFound(@Body WMSCoreMessage oRequest);
    @POST("Outbound/MarkMaterialDamaged")
    Call <String> MarkMaterialDamaged(@Body WMSCoreMessage oRequest);
    @POST("Outbound/GetPickItem")
    Call <String> GetPickItem(@Body WMSCoreMessage oRequest);
    @POST("Outbound/FetchInventoryForLoadSheet")
    Call <String> FetchInventoryForLoadSheet(@Body WMSCoreMessage oRequest);
    @POST("Outbound/ConfirmLoading")
    Call <String> ConfirmLoading(@Body WMSCoreMessage oRequest);
    @POST("Outbound/ConfirmRevertLoading")
    Call <String> ConfirmRevertLoading(@Body WMSCoreMessage oRequest);

    @POST("Outbound/RevertLoading")
    Call <String> RevertLoading(@Body WMSCoreMessage oRequest);
    @POST("Outbound/LoadingComplete")
    Call <String> LoadingComplete(@Body WMSCoreMessage oRequest);
    @POST("Outbound/ValidateLocationAtPicking")
    Call <String>  ValidateLocationAtPicking(@Body WMSCoreMessage oRequest);
    @POST("Outbound/ValidatePalletAtPicking")
    Call <String>  ValidatePalletAtPicking(@Body WMSCoreMessage oRequest);
    @POST("DeNesting/GetDenestingJobOrders")
    Call <String> GetDenestingJobOrders(@Body WMSCoreMessage oRequest);
    @POST("DeNesting/ValidateDenestingLocationCode")
    Call <String> ValidateDenestingLocationCode(@Body WMSCoreMessage oRequest);
    @POST("DeNesting/ValidateDenestingPalletCode")
    Call <String> ValidateDenestingPalletCode(@Body WMSCoreMessage oRequest);
    @POST("DeNesting/UpdateDenestingItem")
    Call <String> UpdateDenestingItem(@Body WMSCoreMessage oRequest);
    @POST("HouseKeeping/ValidateHouseKeepingLocationCode")
    Call <String> ValidateHouseKeepingLocationCode(@Body WMSCoreMessage oRequest);
    @POST("HouseKeeping/ChangeItemMRP")
    Call <String> ChangeItemMRP(@Body WMSCoreMessage oRequest);
    @POST("Inbound/FetchAllPutAwaySuggesions")
    Call <String> FetchAllPutAwaySuggesions(@Body WMSCoreMessage oRequest);
    @POST("HouseKeeping/GetActivestock")
    Call <String> GetActivestock(@Body WMSCoreMessage oRequest);
    @POST("Transfers/ValidateTransferLocationCode")
    Call <String> ValidateTransferLocationCode(@Body WMSCoreMessage oRequest);
    @POST("Transfers/ValidateTransferPalletCode")
    Call <String> ValidateTransferPalletCode(@Body WMSCoreMessage oRequest);
    @POST("Transfers/UpdateInternalTransfer")
    Call <String> UpdateInternalTransfer(@Body WMSCoreMessage oRequest);
    @POST("Exception/LogException")
    Call <String> LogException(@Body WMSCoreMessage oRequest);
    @POST("HouseKeeping/GetActivestockWithOutRSN")
    Call <String> GetActivestockWithOutRSN(@Body WMSCoreMessage oRequest);
    @POST("HouseKeeping/ValidateHouseKeepingPalletCode")
    Call <String> ValidateHouseKeepingPalletCode(@Body WMSCoreMessage oRequest);
    @POST("DeNesting/FetchDenstingJobItemsList")
    Call <String> FetchDenstingJobItemsList(@Body WMSCoreMessage oRequest);
    @POST("Outbound/SkipSuggestedItem")
    Call <String> SkipSuggestedItem(@Body WMSCoreMessage oRequest);
    @POST("Outbound/GetSkipReason")
    Call <String> GetSkipReason(@Body WMSCoreMessage oRequest);
    @POST("HouseKeeping/GetRSNTrackingData")
    Call <String> GetRSNTrackingData(@Body WMSCoreMessage oRequest);
    @POST("Outbound/GetSortList")
    Call <String> GetSortList(@Body WMSCoreMessage oRequest);
    @POST("Outbound/GetConsPLAndTTSList")
    Call <String> GetConsPLAndTTSList(@Body WMSCoreMessage oRequest);

}